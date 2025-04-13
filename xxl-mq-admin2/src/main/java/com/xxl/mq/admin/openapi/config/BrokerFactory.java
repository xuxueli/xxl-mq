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
                 * 1、AppName + Topic：自动初始化
                 *      - 处理增量：30s内在线节点
                 *      - 初始化：
                 *          - appname：注意去重、判断是否存在；
                 *          - topic：默认同appname下 topic 信息相同；处理其中一个 appname 下topic即可；
                 */
                List<Instance> instanceList = instanceMapper.queryOnlineInstance(DateTool.addMilliseconds(new Date(), -3 * BEAT_TIME_INTERVAL));
                if (CollectionTool.isNotEmpty(instanceList)) {

                    // exist appname
                    List<Application> existApplicationData = applicationMapper.findAll();
                    List<String> existAppNameList = existApplicationData.stream().map(Application::getAppname).distinct().collect(Collectors.toList());

                    // exist topic
                    List<Topic> existTopicData = topicMapper.queryByStatus(TopicStatusEnum.NORMAL.getValue());
                    List<String> existTopicNameList = existTopicData.stream().map(Topic::getTopic).distinct().collect(Collectors.toList());

                    // do init
                    for (Instance instance : instanceList) {

                        // avoid repeat
                        if (existAppNameList.contains(instance.getAppname())) {
                            continue;
                        }

                        // do init
                        RegistryRequest registryRequest = GsonTool.fromJson(instance.getRegistryData(), RegistryRequest.class);
                        if (!existAppNameList.contains(instance.getAppname())) {
                            existAppNameList.add(instance.getAppname());

                            // a、init appname
                            Application application = new Application();
                            application.setAppname(instance.getAppname());
                            application.setName(instance.getAppname()+"服务");
                            application.setDesc("初始化数据");
                            application.setRegistryData(null);

                            applicationMapper.insertIgnoreRepeat(application);

                            // b、init topic
                            for (String topicName : registryRequest.getTopicGroup().keySet()) {
                                if (!existTopicNameList.contains(topicName)) {
                                    existTopicNameList.add(topicName);

                                    // init topic
                                    Topic topic = new Topic();
                                    topic.setAppname(instance.getAppname());
                                    topic.setTopic(topicName);
                                    topic.setDesc("初始化数据");
                                    topic.setOwner("系统");
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

                }

                // 2、topic 缓存处理 （查询全部）
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

                // 3、appname 维度缓存：ApplicationRegistryData 缓存处理 （查询全部）
                Map<String, ApplicationRegistryData> applicationRegistryDataStoreNew = new ConcurrentHashMap<>();
                if (CollectionTool.isNotEmpty(instanceList)) {
                    // group by appname
                    Map<String, List<Instance>> instanceListGroup = instanceList.stream().collect(Collectors.groupingBy(Instance::getAppname));
                    for (String appname : instanceListGroup.keySet()) {
                        List<Instance> instanceListGroupAppname = instanceListGroup.get(appname);

                        // instance
                        List<String> instanceUuidList =instanceListGroupAppname.stream().map(Instance::getUuid).sorted().collect(Collectors.toList());
                        Map<String, PartitionUtil.PartitionRange> instancePartitionRange = PartitionUtil.allocatePartition(instanceUuidList);

                        // topic
                        RegistryRequest registryRequest = GsonTool.fromJson(instanceListGroupAppname.get(0).getRegistryData(), RegistryRequest.class);
                        Map<String, Set<String>> topicGroup = registryRequest.getTopicGroup();

                        // build new cache
                        ApplicationRegistryData applicationRegistryData = new ApplicationRegistryData();
                        applicationRegistryData = new ApplicationRegistryData();
                        applicationRegistryData.setTopicGroup(topicGroup);
                        applicationRegistryData.setInstancePartitionRange(instancePartitionRange);

                        applicationRegistryDataStoreNew.put(appname, applicationRegistryData);
                    }
                }
                String applicationRegistryDataNewJson = GsonTool.toJson(applicationRegistryDataStoreNew);
                if (!applicationRegistryDataNewJson.equals(GsonTool.toJson(applicationRegistryDataStore))) {
                    applicationRegistryDataStore = applicationRegistryDataStoreNew;
                    logger.info(">>>>>>>>>>> xxl-mq, registryLocalCacheThread found diff data, applicationRegistryDataNew:{}", applicationRegistryDataNewJson);
                }

            }
        }, BEAT_TIME_INTERVAL, true);
        registryLocalCacheThread.start();
    }

    /**
     * find topic group
     *
     * @param topic
     * @return
     */
    public Set<String> findTopicGroup(String topic) {
        Topic topicData = topicStore.get(topic);
        if (topicData == null) {
            return null;
        }

        ApplicationRegistryData applicationRegistryData = applicationRegistryDataStore.get(topicData.getAppname());
        if (applicationRegistryData == null) {
            return null;
        }
        Map<String, Set<String>> topicGroup = applicationRegistryData.getTopicGroup();
        if (topicGroup == null) {
            return null;
        }

        return topicGroup.get(topic);
    }

    /**
     * find partition range
     *
     * @param topic
     * @return
     */
    public PartitionUtil.PartitionRange findPartitionRange(String topic) {
        Topic topicData = topicStore.get(topic);
        if (topicData == null) {
            return null;
        }

        ApplicationRegistryData applicationRegistryData = applicationRegistryDataStore.get(topicData.getAppname());
        if (applicationRegistryData == null) {
            return null;
        }
        Map<String, PartitionUtil.PartitionRange> instancePartitionRange = applicationRegistryData.getInstancePartitionRange();
        if (instancePartitionRange == null) {
            return null;
        }
        return instancePartitionRange.get(topicData.getAppname());
    }

}
