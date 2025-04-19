package com.xxl.mq.admin.openapi.config;

import com.xxl.mq.admin.constant.enums.*;
import com.xxl.mq.admin.mapper.*;
import com.xxl.mq.admin.model.dto.ApplicationRegistryData;
import com.xxl.mq.admin.model.entity.AccessToken;
import com.xxl.mq.admin.model.entity.Application;
import com.xxl.mq.admin.model.entity.Instance;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.util.PartitionUtil;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.RegistryRequest;
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

    public static BrokerFactory getInstance() {
        return instance;
    }


    // ---------------------- resource ----------------------

    @Resource
    private BrokerService brokerService;
    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private AccessTokenMapper accessTokenMapper;
    @Resource
    private InstanceMapper instanceMapper;
    @Autowired
    private TopicMapper topicMapper;

    // ---------------------- start / stop ----------------------

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
                        // instance
                        Instance newInstance = new Instance();
                        newInstance.setAppname(registryRequest.getAppname());
                        newInstance.setUuid(registryRequest.getInstanceUuid());
                        newInstance.setRegisterHeartbeat(new Date());

                        instanceMapper.insertOrUpdate(newInstance);

                        // init Application
                        if (!applicationRegistryDataStore.containsKey(registryRequest.getAppname())) {
                            Application application = new Application();
                            application.setAppname(registryRequest.getAppname());
                            application.setName(registryRequest.getAppname()+"服务");
                            application.setDesc("初始化生成");
                            application.setRegistryData(null);

                            applicationMapper.insertIgnoreRepeat(application);
                        }

                        // init topic
                        if (CollectionTool.isNotEmpty(registryRequest.getTopicList())) {
                            for (String topicName : registryRequest.getTopicList()) {
                                if (!topicStore.containsKey(topicName)) {
                                    Topic topic = new Topic();
                                    topic.setAppname(registryRequest.getAppname());
                                    topic.setTopic(topicName);
                                    topic.setDesc("初始化生成");
                                    topic.setOwner("未知");
                                    topic.setAlarmEmail(null);
                                    topic.setStatus(TopicStatusEnum.NORMAL.getValue());
                                    topic.setStoreStrategy(StoreStrategyEnum.UNITY_STORE.getValue());
                                    topic.setArchiveStrategy(ArchiveStrategyEnum.RESERVE_7_DAY.getValue());
                                    topic.setPartitionStrategy(PartitionRouteStrategyEnum.HASH.getValue());
                                    topic.setLevel(TopicLevelStrategyEnum.LEVEL_1.getValue());
                                    topic.setRetryStrategy(RetryStrategyEnum.FIXED_RETREAT.getValue());
                                    topic.setRetryCount(0);
                                    topic.setRetryInterval(0);
                                    topic.setExecutionTimeout(-1);

                                    topicMapper.insertIgnoreRepeat(topic);
                                }
                            }
                        }
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

    // ---------------------- LocalCache（Topic + Application） ----------------------

    private volatile Map<String, Topic> topicStore = new ConcurrentHashMap<>();
    private volatile Map<String, ApplicationRegistryData> applicationRegistryDataStore = new ConcurrentHashMap<>();

    private void startRegistryLocalCacheThread() {
        CyclicThread registryLocalCacheThread = new CyclicThread("registryLocalCacheThread", true, new Runnable() {
            @Override
            public void run() {

                // 1、topic 缓存信息
                List<Topic> topicList =topicMapper.queryByStatus(TopicStatusEnum.NORMAL.getValue());
                Map<String, Topic> topicStoreNew = new ConcurrentHashMap<>();
                if (CollectionTool.isNotEmpty(topicList)) {
                    topicList.forEach(topic -> {
                        topicStoreNew.put(topic.getTopic(), topic);
                    });
                }
                String topicStoreNewJson = GsonTool.toJson(topicStoreNew);
                if (!topicStoreNewJson.equals(GsonTool.toJson(topicStore))) {
                    topicStore = topicStoreNew;
                    logger.info(">>>>>>>>>>> xxl-mq, registryLocalCacheThread found diff data, topicStoreNew:{}", topicStoreNewJson);
                }

                // 2、appname 缓存信息
                List<Instance> instanceList = instanceMapper.queryOnlineInstance(DateTool.addMilliseconds(new Date(), -3 * BEAT_TIME_INTERVAL));
                Map<String, ApplicationRegistryData> applicationRegistryDataStoreNew = new ConcurrentHashMap<>();
                if (CollectionTool.isNotEmpty(instanceList)) {
                    // group by appname
                    Map<String, List<Instance>> instanceListGroup = instanceList.stream().collect(Collectors.groupingBy(Instance::getAppname));
                    for (String appname : instanceListGroup.keySet()) {
                        // instance uuid list, sorted
                        List<Instance> instanceListGroupAppname = instanceListGroup.get(appname);
                        List<String> instanceUuidList =instanceListGroupAppname.stream().map(Instance::getUuid).sorted().collect(Collectors.toList());

                        // partition of uuid
                        TreeMap<String, PartitionUtil.PartitionRange> instancePartitionRange = PartitionUtil.allocatePartition(instanceUuidList);

                        // build cache data
                        ApplicationRegistryData applicationRegistryData = new ApplicationRegistryData();
                        applicationRegistryData = new ApplicationRegistryData();
                        applicationRegistryData.setInstancePartitionRange(instancePartitionRange);

                        applicationRegistryDataStoreNew.put(appname, applicationRegistryData);
                    }
                }
                String applicationRegistryDataNewJson = GsonTool.toJson(applicationRegistryDataStoreNew);
                if (!applicationRegistryDataNewJson.equals(GsonTool.toJson(applicationRegistryDataStore))) {
                    applicationRegistryDataStore = applicationRegistryDataStoreNew;
                    logger.info(">>>>>>>>>>> xxl-mq, registryLocalCacheThread found diff data, applicationRegistryDataNew:{}", applicationRegistryDataNewJson);
                }

                // write registry_data for appname
                List<Application> applicationList = applicationMapper.findAll();
                for (Application application : applicationList) {
                    ApplicationRegistryData registryDataNew = applicationRegistryDataStore.get(application.getAppname());
                    String registryDataNewJson = registryDataNew!=null?GsonTool.toJson(registryDataNew):"";
                    if (!registryDataNewJson.equals(application.getRegistryData())) {
                        // do update
                        application.setRegistryData(registryDataNewJson);
                        applicationMapper.updateRegistryData(application);
                    }
                }

            }
        }, BEAT_TIME_INTERVAL, true);
        registryLocalCacheThread.start();
    }

    /**
     * find partition range by topic
     *
     * @param topic
     * @return
     */
    public PartitionUtil.PartitionRange findPartitionRangeByTopic(String topic) {
        Topic topicData = topicStore.get(topic);
        if (topicData == null) {
            return null;
        }
        return findPartitionRangeByAppname(topicData.getTopic());
    }

    /**
     * find partition range by appname
     *
     * @param appname
     * @return
     */
    public PartitionUtil.PartitionRange findPartitionRangeByAppname(String appname) {
        if (appname == null) {
            return null;
        }

        ApplicationRegistryData applicationRegistryData = applicationRegistryDataStore.get(appname);
        if (applicationRegistryData == null) {
            return null;
        }

        Map<String, PartitionUtil.PartitionRange> instancePartitionRange = applicationRegistryData.getInstancePartitionRange();
        return instancePartitionRange.get(appname);
    }

}
