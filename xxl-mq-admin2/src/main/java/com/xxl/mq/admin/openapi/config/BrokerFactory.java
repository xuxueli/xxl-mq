package com.xxl.mq.admin.openapi.config;

import com.xxl.mq.admin.constant.enums.AccessTokenStatuEnum;
import com.xxl.mq.admin.constant.enums.TopicStatusEnum;
import com.xxl.mq.admin.mapper.*;
import com.xxl.mq.admin.model.entity.AccessToken;
import com.xxl.mq.admin.model.entity.Application;
import com.xxl.mq.admin.model.entity.Instance;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.RegistryRequest;
import com.xxl.mq.core.openapi.model.broker.ApplicationRegistryData;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.concurrent.MessageQueue;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.jsonrpc.JsonRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * registry config
 *
 * @author xuxueli
 */
@Configuration
public class BrokerFactory implements InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(BrokerFactory.class);


    // ---------------------- instance ----------------------
    private static BrokerFactory instance;
    @Autowired
    private TopicMapper topicMapper;

    public static BrokerFactory getInstance() {
        return instance;
    }


    // ---------------------- start / stop ----------------------

    @Resource
    private BrokerService brokerService;
    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private AccessTokenMapper accessTokenMapper;
    @Resource
    private InstanceMapper instanceMapper;


    @Override
    public void afterPropertiesSet() throws Exception {
        // base init
        instance = this;

        // 1、AccessTokenThread
        startAccessTokenThread();

        // 2、Registry MessageQueue
        startRegistryMessageQueue();

        // 3、Registry LocalCache Thread
        startRegistryLocalCacheThread();
    }

    @Override
    public void destroy() throws Exception {
        // stop
    }


    // ---------------------- openapi JsonRpcServer ----------------------

    @Bean
    public JsonRpcServer jsonRpcServer() {
        JsonRpcServer jsonRpcServer = new JsonRpcServer();
        jsonRpcServer.register("brokerService", brokerService);

        return jsonRpcServer;
    }


    // ---------------------- AccessToken ----------------------

    /**
     * AccessToken LocalStore
     */
    private volatile Set<String> accessTokenStore = new ConcurrentSkipListSet<>();

    /**
     * beat time interval, 30s
     */
    private static final int BEAT_TIME_INTERVAL = 30 * 1000;

    /**
     * start AccessTokenThread (will stop with jvm)
     */
    private void startAccessTokenThread() {
        CyclicThread accessTokenThread = new CyclicThread("accessTokenThread", true, new Runnable() {
            @Override
            public void run() {
                try {
                    // build new data
                    ConcurrentSkipListSet<String> accessTokenStoreNew = new ConcurrentSkipListSet<>();

                    // query valid accesstoken data
                    List<AccessToken> accessTokenList = accessTokenMapper.queryValidityAccessToken(AccessTokenStatuEnum.NORMAL.getValue());
                    if (CollectionTool.isNotEmpty(accessTokenList)) {
                        accessTokenStoreNew.addAll(accessTokenList.stream()
                                .map(AccessToken::getAccessToken)
                                .collect(Collectors.toSet()));
                    }

                    // do refresh
                    String newDataJson = GsonTool.toJson(accessTokenStoreNew);
                    if (!Objects.equals(newDataJson, GsonTool.toJson(accessTokenStore))) {
                        logger.info(">>>>>>>>>>> xxl-mq, accessTokenThread found diff data, accessTokenStoreNew:{}", newDataJson);
                        accessTokenStore = accessTokenStoreNew;
                    }
                } catch (Exception e) {
                    logger.error("AccessTokenHelpler error:", e);
                }
            }
        }, BEAT_TIME_INTERVAL, true);
        accessTokenThread.start();
    }

    /**
     * valid  AccessToken
     *
     * @param accessToken
     * @return
     */
    public boolean validAccessToken(String accessToken) {
        return accessToken!=null && accessTokenStore.contains(accessToken);
    }

    // ---------------------- Registry MessageQueue ----------------------

    private volatile MessageQueue<RegistryRequest> registryMessageQueue;

    /**
     * start Registry MessageQueue (will stop with jvm)
     */
    private void startRegistryMessageQueue() {
        registryMessageQueue = new MessageQueue<RegistryRequest>(
                "registryMessageQueue",
                1000,
                messages -> {
                    for (RegistryRequest registryRequest : messages) {
                        // registry data
                        RegistryRequest registryData = new RegistryRequest();
                        registryData.setTopicGroup(registryRequest.getTopicGroup());

                        String registryDate = GsonTool.toJson(registryData);

                        // instance
                        Instance newInstance = new Instance();
                        newInstance.setAppname(registryRequest.getAppname());
                        newInstance.setUuid(registryRequest.getInstanceUuid());
                        newInstance.setRegisterHeartbeat(new Date());
                        newInstance.setRegistryData(registryDate);

                        instanceMapper.insertOrUpdate(newInstance);
                    }
                },
                1,
                1);
    }

    /**
     * registry
     *
     * @param registryRequest
     * @return
     */
    public boolean registry(RegistryRequest registryRequest) {
        return registryMessageQueue.produce(registryRequest);
    }

    // ---------------------- Registry LocalCache ----------------------

    private volatile Map<String, Topic> topicStore = new ConcurrentHashMap<>();
    private volatile Map<String, ApplicationRegistryData> applicationRegistryDataStore = new ConcurrentHashMap<>();

    private void startRegistryLocalCacheThread() {
        CyclicThread registryLocalCacheThread = new CyclicThread("registryLocalCacheThread", true, new Runnable() {
            @Override
            public void run() {

                /**
                 * 1、Instance 初始化
                 *      - 处理增量，30s内在线节点
                 *      - 不存在直接初始化:
                 *          - appname： 去重新建
                 *          - topic：默认同appname下 topic 信息相同；取最新instance下topic识别增量、新建；
                 */
                List<Instance> instanceList = instanceMapper.queryOnlineInstance(DateTool.addMilliseconds(new Date(), -3 * BEAT_TIME_INTERVAL));
                if (CollectionTool.isNotEmpty(instanceList)) {
                    for (Instance instance : instanceList) {

                        // todo，多虑已存在，新建；
                        Application application = new Application();
                        application.setAppname(instance.getAppname());
                        application.setName("初始化");
                        application.setDesc("初始化");
                        application.setRegistryData(null);

                        applicationMapper.insert(application);
                    }
                }
                // todo 新建

                // 3、topic 缓存处理 （查询全部）
                List<Topic> topicList =topicMapper.queryByStatus(TopicStatusEnum.NORMAL.getValue());
                Map<String, Topic> topicStoreNew = new ConcurrentHashMap<>();
                if (CollectionTool.isNotEmpty(topicList)) {
                    topicList.forEach(topic -> {
                        topicStoreNew.put(topic.getTopic(), topic);
                    });
                }
                topicStore = topicStoreNew;

                // 4、ApplicationRegistryData 缓存处理 （查询全部）

            }
        }, BEAT_TIME_INTERVAL, true);
        registryLocalCacheThread.start();
    }

}
