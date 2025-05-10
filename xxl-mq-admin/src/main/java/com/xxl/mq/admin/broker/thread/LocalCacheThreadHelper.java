package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerBootstrap;
import com.xxl.mq.admin.constant.enums.TopicStatusEnum;
import com.xxl.mq.admin.model.dto.ApplicationRegistryData;
import com.xxl.mq.admin.model.entity.Application;
import com.xxl.mq.admin.model.entity.Instance;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.util.PartitionUtil;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.http.IPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.xxl.mq.admin.broker.thread.AccessTokenThreadHelper.BEAT_TIME_INTERVAL;

/**
 * registry local cache thread
 *
 * @author xuxueli
 */
public class LocalCacheThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger(LocalCacheThreadHelper.class);

    // ---------------------- init ----------------------

    private final BrokerBootstrap brokerBootstrap;
    public LocalCacheThreadHelper(BrokerBootstrap brokerBootstrap) {
        this.brokerBootstrap = brokerBootstrap;
    }

    // ---------------------- start / stop ----------------------

    private volatile Map<String, Topic> topicStore = new ConcurrentHashMap<>();
    private volatile Map<String, Application> applicationStore = new ConcurrentHashMap<>();
    private volatile Map<String, ApplicationRegistryData> applicationRegistryDataStore = new ConcurrentHashMap<>();

    private final String brokerAppname = "xxl-mq-admin";
    private String brokerUuid = null;

    /**
     * start
     *
     * remark：
     *      1、topic缓存：DB > cache
     *      2.1、Broker 注册写 Instance：refresh DB（For 集群节点感知）
     *      2.2、过期清理 Instance：DB 清理
     *      3、Instance注册信息，构建缓存：DB > cache
     *      4、Instance注册信息，写入Application：cache 》 DB
     *      5、Application缓存：DB > cache
     */
    public void start(){
        // init param
        brokerUuid = IPTool.getIp() + ":"+ brokerBootstrap.getPort();

        // start thread
        CyclicThread registryLocalCacheThread = new CyclicThread("registryLocalCacheThread", true, new Runnable() {
            @Override
            public void run() {

                // 1、topic缓存：DB > cache
                List<Topic> topicList = brokerBootstrap.getTopicMapper().queryByStatus(TopicStatusEnum.NORMAL.getValue());
                Map<String, Topic> topicStoreNew = new ConcurrentHashMap<>();
                if (CollectionTool.isNotEmpty(topicList)) {
                    topicList.forEach(topic -> {
                        topicStoreNew.put(topic.getTopic(), topic);
                    });
                }
                String topicStoreNewJson = GsonTool.toJson(topicStoreNew);
                if (!topicStoreNewJson.equals(GsonTool.toJson(topicStore))) {
                    topicStore = topicStoreNew;
                    logger.info(">>>>>>>>>>> xxl-mq, RegistryLocalCacheThreadHelper found diff data, topicStoreNew:{}", topicStoreNewJson);
                }

                // 2.1、Broker 注册写 Instance：refresh DB（For 集群节点感知）
                Instance newInstance = new Instance();
                newInstance.setAppname(brokerAppname);
                newInstance.setUuid(brokerUuid);
                newInstance.setRegisterHeartbeat(new Date());
                brokerBootstrap.getInstanceMapper().insertOrUpdate(newInstance);

                // 2.2、过期清理 Instance：DB 清理
                brokerBootstrap.getInstanceMapper().deleteOfflineInstance(DateTool.addMilliseconds(new Date(), -3 * BEAT_TIME_INTERVAL));

                // 3、Instance注册信息，构建缓存：DB > cache
                List<Instance> instanceList = brokerBootstrap.getInstanceMapper().queryOnlineInstance(DateTool.addMilliseconds(new Date(), -3 * BEAT_TIME_INTERVAL));
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

                // 4、Instance注册信息，写入Application：cache 》 DB
                List<Application> applicationList = brokerBootstrap.getApplicationMapper().findAll();
                Map<String, Application> applicationStoreNew = new ConcurrentHashMap<>();
                for (Application application : applicationList) {
                    // check and refresh registry_data
                    ApplicationRegistryData registryDataNew = applicationRegistryDataStore.get(application.getAppname());
                    String registryDataNewJson = registryDataNew!=null?GsonTool.toJson(registryDataNew):"";
                    if (!registryDataNewJson.equals(application.getRegistryData())) {
                        // do update
                        application.setRegistryData(registryDataNewJson);
                        brokerBootstrap.getApplicationMapper().updateRegistryData(application);
                    }

                    // build cache data
                    applicationStoreNew.put(application.getAppname(), application);
                }

                // 5、Application缓存：DB > cache
                String applicationStoreNewJson = GsonTool.toJson(applicationStoreNew);
                if (!applicationStoreNewJson.equals(GsonTool.toJson(applicationStore))) {
                    applicationStore = applicationStoreNew;
                    logger.info(">>>>>>>>>>> xxl-mq, RegistryLocalCacheThreadHelper found diff data, applicationStoreNewJson:{}", applicationStoreNewJson);
                }

            }
        }, BEAT_TIME_INTERVAL, true);
        registryLocalCacheThread.start();
    }

    public void stop(){
        // do nothing
    }

    // ---------------------- tool ----------------------

    /**
     * find topic
     *
     * @param topic
     * @return
     */
    public Topic findTopic(String topic) {
        return topicStore.get(topic);
    }

    /**
     * find topic all
     *
     * @return
     */
    public List<Topic> findTopicAll() {
        return new ArrayList<>(topicStore.values());
    }

    /**
     * find application
     *
     * @param appname
     * @return
     */
    public Application findApplication(String appname) {
        return applicationStore.get(appname);
    }

    /**
     * find partition range by appname
     *
     * @param appname
     * @return
     */
    public TreeMap<String, PartitionUtil.PartitionRange> findPartitionRangeByAppname(String appname) {
        if (appname == null) {
            return null;
        }

        ApplicationRegistryData applicationRegistryData = applicationRegistryDataStore.get(appname);
        if (applicationRegistryData == null) {
            return null;
        }

        TreeMap<String, PartitionUtil.PartitionRange> instancePartitionRange = applicationRegistryData.getInstancePartitionRange();
        return instancePartitionRange;
    }

    /**
     * find partition range by topic
     *
     * @param topic
     * @return
     */
    public TreeMap<String, PartitionUtil.PartitionRange> findPartitionRangeByTopic(String topic) {
        Topic topicData = topicStore.get(topic);
        if (topicData == null) {
            return null;
        }
        return findPartitionRangeByAppname(topicData.getAppname());
    }

    /**
     * find partition range by appname and instanceUuid
     *
     * @param appname
     * @param instanceUuid
     * @return
     */
    public PartitionUtil.PartitionRange findPartitionRangeByAppnameAndUuid(String appname, String instanceUuid) {
        Map<String, PartitionUtil.PartitionRange> instancePartitionRange = findPartitionRangeByAppname(appname);
        if (instancePartitionRange == null) {
            return null;
        }
        return instancePartitionRange.get(instanceUuid);
    }

    /**
     * is master broker node
     *
     * @return
     */
    public boolean isMasterBroker(){
        TreeMap<String, PartitionUtil.PartitionRange> instancePartitionRange = findPartitionRangeByAppname("xxl-mq-admin");
        return MapTool.isNotEmpty(instancePartitionRange) && brokerUuid.equals(instancePartitionRange.firstEntry().getKey());
    }

}
