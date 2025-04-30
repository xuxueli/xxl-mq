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
import com.xxl.tool.gson.GsonTool;
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

    /**
     * start
     *
     * remark：
     *      1、topic：缓存信息
     *      2、注册Instance：缓存信息
     *      3、appname：缓存信息
     *      4、appname 注册信息更新（步骤2信息 write）
     */
    public void start(){
        CyclicThread registryLocalCacheThread = new CyclicThread("registryLocalCacheThread", true, new Runnable() {
            @Override
            public void run() {

                // 1、topic：缓存信息
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

                // 2、注册Instance：缓存信息
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

                /**
                 * 3、appname：缓存信息
                 * 4、appname 注册信息更新
                 */
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
    public Map<String, PartitionUtil.PartitionRange> findPartitionRangeByAppname(String appname) {
        if (appname == null) {
            return null;
        }

        ApplicationRegistryData applicationRegistryData = applicationRegistryDataStore.get(appname);
        if (applicationRegistryData == null) {
            return null;
        }

        Map<String, PartitionUtil.PartitionRange> instancePartitionRange = applicationRegistryData.getInstancePartitionRange();
        return instancePartitionRange;
    }

    /**
     * find partition range by topic
     *
     * @param topic
     * @return
     */
    public Map<String, PartitionUtil.PartitionRange> findPartitionRangeByTopic(String topic) {
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

}
