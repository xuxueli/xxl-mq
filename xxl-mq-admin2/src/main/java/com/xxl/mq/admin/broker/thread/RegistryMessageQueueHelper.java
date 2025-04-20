package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerFactory;
import com.xxl.mq.admin.constant.enums.*;
import com.xxl.mq.admin.model.entity.Application;
import com.xxl.mq.admin.model.entity.Instance;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.core.openapi.model.RegistryRequest;
import com.xxl.tool.concurrent.MessageQueue;
import com.xxl.tool.core.CollectionTool;

import java.util.Date;

/**
 * registry message queue
 *
 * @author xuxueli
 */
public class RegistryMessageQueueHelper {

    // ---------------------- init ----------------------

    private final BrokerFactory brokerFactory;
    public RegistryMessageQueueHelper(BrokerFactory brokerFactory) {
        this.brokerFactory = brokerFactory;
    }

    // ---------------------- start / stop ----------------------

    private volatile MessageQueue<RegistryRequest> registryMessageQueue;
    private volatile MessageQueue<RegistryRequest> registryRemoveMessageQueue;

    /**
     * start Registry MessageQueue (will stop with jvm)
     */
    public void start(){
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

                        brokerFactory.getInstanceMapper().insertOrUpdate(newInstance);

                        // init Application
                        if (brokerFactory.getRegistryLocalCacheThreadHelper().findApplication(registryRequest.getAppname()) == null) {
                            Application application = new Application();
                            application.setAppname(registryRequest.getAppname());
                            application.setName(registryRequest.getAppname()+"服务");
                            application.setDesc("初始化生成");
                            application.setRegistryData(null);

                            brokerFactory.getApplicationMapper().insertIgnoreRepeat(application);
                        }

                        // init topic
                        if (CollectionTool.isNotEmpty(registryRequest.getTopicList())) {
                            for (String topicName : registryRequest.getTopicList()) {
                                if (brokerFactory.getRegistryLocalCacheThreadHelper().findTopic(topicName) == null) {
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

                                    brokerFactory.getTopicMapper().insertIgnoreRepeat(topic);
                                }
                            }
                        }
                    }
                },
                1,
                1);

        registryRemoveMessageQueue = new MessageQueue<RegistryRequest>(
                "registryRemoveMessageQueue",
                1000,
                messages -> {
                    for (RegistryRequest registryRequest : messages) {
                        // instance
                        Instance instance = new Instance();
                        instance.setAppname(registryRequest.getAppname());
                        instance.setUuid(registryRequest.getInstanceUuid());

                        brokerFactory.getInstanceMapper().deleteInstance(instance);
                    }
                },
                1,
                1);
    }


    public void stop(){
        // do nothing
    }

    // ---------------------- tool ----------------------

    /**
     * registry
     *
     * @param registryRequest
     * @return
     */
    public boolean registry(RegistryRequest registryRequest) {
        return registryMessageQueue.produce(registryRequest);
    }

    /**
     * registry
     *
     * @param registryRequest
     * @return
     */
    public boolean registryRemove(RegistryRequest registryRequest) {
        return registryRemoveMessageQueue.produce(registryRequest);
    }

}
