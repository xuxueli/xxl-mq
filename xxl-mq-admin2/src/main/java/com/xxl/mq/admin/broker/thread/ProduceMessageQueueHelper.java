package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerFactory;
import com.xxl.mq.admin.constant.enums.MessageStatusEnum;
import com.xxl.mq.admin.constant.enums.PartitionRouteStrategyEnum;
import com.xxl.mq.admin.constant.enums.RetryStrategyEnum;
import com.xxl.mq.admin.model.entity.Message;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.util.PartitionUtil;
import com.xxl.mq.core.openapi.model.ConsumeRequest;
import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.mq.core.openapi.model.ProduceRequest;
import com.xxl.mq.core.openapi.model.PullRequest;
import com.xxl.mq.core.util.ConsumeLogUtil;
import com.xxl.tool.concurrent.MessageQueue;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.response.Response;

import java.util.*;
import java.util.stream.Collectors;

/**
 * prude message queue helper
 *
 * @author xuxueli
 */
public class ProduceMessageQueueHelper {

    // ---------------------- init ----------------------

    private final BrokerFactory brokerFactory;
    public ProduceMessageQueueHelper(BrokerFactory brokerFactory) {
        this.brokerFactory = brokerFactory;
    }

    // ---------------------- start / stop ----------------------

    private volatile MessageQueue<ProduceRequest> produceMessageQueue;
    private volatile MessageQueue<ConsumeRequest> consumeMessageQueue;

    public void start(){
        produceMessageQueue = new MessageQueue<ProduceRequest>(
                "produceMessageQueue",
                2000,
                messages -> {
                    // 1、collect message
                    List<MessageData> validMessageTotal = new ArrayList<>();
                    for (ProduceRequest produceRequest : messages) {
                        // valid
                        if (CollectionTool.isNotEmpty(produceRequest.getMessageList())) {
                            for (MessageData messageData : produceRequest.getMessageList()) {
                                if (StringTool.isBlank(messageData.getTopic())
                                        || StringTool.isBlank(messageData.getData())) {
                                    continue;
                                }
                                validMessageTotal.add(messageData);
                            }
                        }
                    }

                    // 2、write message, batch type
                    for (List<MessageData> validMessageDataList : CollectionTool.split(validMessageTotal, 50)) {
                        List<Message> messageList = new ArrayList<>();
                        for (MessageData messageData : validMessageDataList) {

                            // 2.1、param
                            Date effectTime = messageData.getEffectTime()>0 ? new Date(messageData.getEffectTime()):new Date();
                            Topic topicData = brokerFactory.getRegistryLocalCacheThreadHelper().findTopic(messageData.getTopic());
                            PartitionRouteStrategyEnum partitionRouteStrategyEnum = PartitionRouteStrategyEnum.match(topicData!=null?topicData.getPartitionStrategy():-1, PartitionRouteStrategyEnum.HASH);
                            Map<String, PartitionUtil.PartitionRange> instancePartitionRange = brokerFactory.getRegistryLocalCacheThreadHelper().findPartitionRangeByTopic(messageData.getTopic());

                            // 2.2、route produce
                            if (PartitionRouteStrategyEnum.BROADCAST==partitionRouteStrategyEnum) {
                                // valid
                                if (MapTool.isEmpty(instancePartitionRange)) {
                                    continue;
                                }

                                for (PartitionUtil.PartitionRange partitionRange : instancePartitionRange.values()) {
                                    int partitionId = partitionRange.getPartitionIdFrom();

                                    // adaptor
                                    Message message = new Message();
                                    message.setTopic(messageData.getTopic());
                                    message.setPartitionId(partitionId);
                                    message.setData(messageData.getData());
                                    message.setStatus(MessageStatusEnum.NEW.getValue());
                                    message.setEffectTime(effectTime);
                                    message.setRetryCountRemain(topicData!=null?topicData.getRetryCount():0);
                                    message.setConsumeLog(ConsumeLogUtil.generateConsumeLog("生产消息(广播)",
                                            "Message="+ GsonTool.toJson(message) +
                                                    " ;instancePartitionRange="+GsonTool.toJson(instancePartitionRange)));
                                    // collect
                                    messageList.add(message);
                                }
                            } else {
                                int partitionId = partitionRouteStrategyEnum.getPartitionRouter().route(messageData.getTopic(), messageData.getPartitionKey(), instancePartitionRange);

                                // adaptor
                                Message message = new Message();
                                message.setTopic(messageData.getTopic());
                                message.setPartitionId(partitionId);
                                message.setData(messageData.getData());
                                message.setStatus(MessageStatusEnum.NEW.getValue());
                                message.setEffectTime(effectTime);
                                message.setRetryCountRemain(topicData!=null?topicData.getRetryCount():0);
                                message.setConsumeLog(ConsumeLogUtil.generateConsumeLog("生产消息", "Message="+GsonTool.toJson(message)));

                                // collect
                                messageList.add(message);
                            }
                        }
                        // produce
                        if (CollectionTool.isEmpty(messageList)) {
                            continue;
                        }
                        brokerFactory.getMessageMapper().batchInsert(messageList);
                        System.out.println(111);
                    }
                },
                50,
                20);

        consumeMessageQueue = new MessageQueue<ConsumeRequest>(
                "consumeMessageQueue",
                1000,
                messages -> {

                    // 1、filter and collect
                    List<MessageData> validMessageTotal = new ArrayList<>();
                    for (ConsumeRequest consumeRequest : messages) {
                        // valid
                        if (CollectionTool.isNotEmpty(consumeRequest.getMessageList())) {
                            for (MessageData messageData : consumeRequest.getMessageList()) {
                                if (messageData.getId() <=0 || messageData.getStatus()<=0) {
                                    continue;
                                }
                                validMessageTotal.add(messageData);
                            }
                        }
                    }
                    if (CollectionTool.isEmpty(validMessageTotal)) {
                        return;
                    }

                    // 2、batch write consume data, collect fail-message
                    List<Message> failMessageList = new ArrayList<>();
                    for (List<MessageData> validMessageDataList : CollectionTool.split(validMessageTotal, 50)) {
                        List<Message> messageList = new ArrayList<>();
                        for (MessageData messageData : validMessageDataList) {

                            // adaptor
                            Message message = new Message();
                            message.setId(messageData.getId());
                            message.setStatus(messageData.getStatus());
                            message.setTopic(messageData.getTopic());
                            message.setConsumeLog(ConsumeLogUtil.HR_TAG+messageData.getConsumeLog());

                            // collect
                            messageList.add(message);

                            // fail-retry collect
                            if (messageData.getStatus()==MessageStatusEnum.EXECUTE_FAIL.getValue()
                                    || messageData.getStatus()==MessageStatusEnum.EXECUTE_TIMEOUT.getValue()) {
                                failMessageList.add(message);
                            }
                        }
                        brokerFactory.getMessageMapper().batchUpdateStatus(messageList);
                    }

                    // 3、fail retry
                    failRetry(failMessageList);

                },
                50,
                20);
    }

    public void stop(){
        // do nothing
    }

    /**
     * fail-retry
     */
    private void failRetry(List<Message> failMessageList) {
        if (CollectionTool.isEmpty(failMessageList)) {
            return;
        }

        /**
         * process fail-retry
         *      1、retryCount：-1
         *      2、status：rollback 2 NEW
         *      3、effectTime
         *
         *             long addSecond = 0;
         *             if (retryStrategyEnum == RetryStrategyEnum.FIXED_RETREAT) {
         *                 addSecond = retryInterval;
         *             } else if (retryStrategyEnum == RetryStrategyEnum.LINEAR_RETREAT) {
         *                 addSecond = retryInterval * (retryCount-retryCount_remain + 1);
         *             } else if (retryStrategyEnum == RetryStrategyEnum.EXPONENTIAL_RETREAT) {
         *                 addSecond = retryInterval * (int)Math.pow(2, (retryCount-retryCount_remain + 1));
         *             }
         */
        // param
        for (Message message : failMessageList) {

            // match topic
            Topic topic = brokerFactory.getRegistryLocalCacheThreadHelper().findTopic(message.getTopic());
            if (topic == null) {
                topic = new Topic();
                topic.setRetryInterval(3);
                topic.setRetryCount(3);
                topic.setRetryStrategy(RetryStrategyEnum.FIXED_RETREAT.getValue());
            }

            // data
            int retryInterval = 3;      // from topic
            int retryCount = 3;         // from topic
            int retryStrategy = -1;     // from topic

            int retryCount_remain = 1;  // from message
            if (retryCount_remain <=0 ) {
                continue;
            }

            // calculate retry info
            RetryStrategyEnum retryStrategyEnum = RetryStrategyEnum.match(retryStrategy, RetryStrategyEnum.FIXED_RETREAT);
            long addSecond = 0;
            if (retryStrategyEnum == RetryStrategyEnum.FIXED_RETREAT) {
                addSecond = retryInterval;
            } else if (retryStrategyEnum == RetryStrategyEnum.LINEAR_RETREAT) {
                addSecond = retryInterval * (retryCount-retryCount_remain + 1);
            } else if (retryStrategyEnum == RetryStrategyEnum.EXPONENTIAL_RETREAT) {
                addSecond = retryInterval * (int)Math.pow(2, (retryCount-retryCount_remain + 1));
            }
            Date newEffectTime = DateTool.addSeconds(new Date(), addSecond);
            int newStatus = MessageStatusEnum.NEW.getValue();
            int changeRetryCount  = -1;     // 限制大于0；

            // write retry data todo
            //message.setRetryCount( -1 );      // RetryCount change；
            message.setStatus(MessageStatusEnum.NEW.getValue());
            message.setEffectTime(newEffectTime);

        }

        // fail retry
        //brokerFactory.getMessageMapper().batchFailRetry(failMessageList);
    }

    // ---------------------- tool ----------------------

    /**
     * produce message
     *
     * @param produceRequest
     * @return
     */
    public boolean produce(ProduceRequest produceRequest) {
        return produceMessageQueue.produce(produceRequest);
    }

    /**
     * produce message
     *
     * @param consumeRequest
     * @return
     */
    public boolean consume(ConsumeRequest consumeRequest) {
        return consumeMessageQueue.produce(consumeRequest);
    }

    /**
     * pull message
     *
     * @param pullRequest
     * @return
     */
    public Response<List<MessageData>> pull(PullRequest pullRequest) {

        // valid
        if (CollectionTool.isEmpty(pullRequest.getTopicList())
                || StringTool.isBlank(pullRequest.getAppname())
                || StringTool.isBlank(pullRequest.getInstanceUuid())) {
            return Response.of(401, "Illegal parameters.");
        }

        // match partition
        PartitionUtil.PartitionRange partitionRange = brokerFactory.getRegistryLocalCacheThreadHelper().findPartitionRangeByAppnameAndUuid(pullRequest.getAppname(), pullRequest.getInstanceUuid());
        if (partitionRange == null) {
            return Response.of(402, "Current instanceUuid has not been assigned a partition.");
        }

        // 1、消息检索
        int pagesize = 10;
        List<Message> messageList = brokerFactory.getMessageMapper().pullQuery(pullRequest.getTopicList(), MessageStatusEnum.NEW.getValue(), partitionRange.getPartitionIdFrom(), partitionRange.getPartitionIdTo(), pagesize);
        if (CollectionTool.isEmpty(messageList)) {
            return Response.ofSuccess();
        }

        // 2、消息锁定, with uuid
        List<Long> messageIdList = messageList.stream().map(Message::getId).collect(Collectors.toList());
        int count = brokerFactory.getMessageMapper().pullLock(messageIdList, pullRequest.getInstanceUuid(), MessageStatusEnum.NEW.getValue(), MessageStatusEnum.RUNNING.getValue());

        // 3、锁定消息检索, with uuid （锁定失败，过滤锁定成功数据）
        if (count < messageList.size()) {
            messageList = brokerFactory.getMessageMapper().pullQueryByUuid(messageIdList, pullRequest.getInstanceUuid(), MessageStatusEnum.RUNNING.getValue());
            if (CollectionTool.isEmpty(messageList)) {
                // lock fail all
                return Response.ofSuccess();
            }
        }

        // adaptor
        List<MessageData> messageDataList = new ArrayList<>();
        for (Message message : messageList) {
            // collect
            messageDataList.add(new MessageData(
                    message.getId(),
                    message.getTopic(),
                    message.getData()));
        }
        return Response.ofSuccess(messageDataList);
    }

}
