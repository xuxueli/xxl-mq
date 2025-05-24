package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerBootstrap;
import com.xxl.mq.core.constant.MessageStatusEnum;
import com.xxl.mq.admin.constant.enums.PartitionRouteStrategyEnum;
import com.xxl.mq.admin.constant.enums.RetryStrategyEnum;
import com.xxl.mq.admin.constant.enums.TopicStatusEnum;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * prude message queue helper
 *
 * @author xuxueli
 */
public class MessageProduceAndConsumeThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger(MessageProduceAndConsumeThreadHelper.class);

    // ---------------------- init ----------------------

    private final BrokerBootstrap brokerBootstrap;
    public MessageProduceAndConsumeThreadHelper(BrokerBootstrap brokerBootstrap) {
        this.brokerBootstrap = brokerBootstrap;
    }

    // ---------------------- start / stop ----------------------

    private volatile MessageQueue<ProduceRequest> produceMessageQueue;
    private volatile MessageQueue<ConsumeRequest> consumeMessageQueue;

    /**
     * start
     *
     * remark：
     *      1、Produce MessageQueue: batch write message to store
     *      2、Consume MessageQueue: 1、batch write consume result to store；2、find fail-message and retry.
     */
    public void start(){
        produceMessageQueue = new MessageQueue<ProduceRequest>(
                "produceMessageQueue",
                5000,
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
                            Topic topicData = brokerBootstrap.getLocalCacheThreadHelper().findTopic(messageData.getTopic());
                            PartitionRouteStrategyEnum partitionRouteStrategyEnum = PartitionRouteStrategyEnum.match(topicData!=null?topicData.getPartitionStrategy():-1, PartitionRouteStrategyEnum.HASH);
                            Map<String, PartitionUtil.PartitionRange> instancePartitionRange = brokerBootstrap.getLocalCacheThreadHelper().findPartitionRangeByTopic(messageData.getTopic());

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
                                    message.setBizId(messageData.getBizId());
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
                                int partitionId = partitionRouteStrategyEnum.getPartitionRouter().route(messageData.getTopic(), messageData.getBizId(), instancePartitionRange);

                                // adaptor
                                Message message = new Message();
                                message.setTopic(messageData.getTopic());
                                message.setPartitionId(partitionId);
                                message.setData(messageData.getData());
                                message.setBizId(messageData.getBizId());
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
                        brokerBootstrap.getMessageMapper().batchInsert(messageList);
                    }
                },
                50,
                50);

        consumeMessageQueue = new MessageQueue<ConsumeRequest>(
                "consumeMessageQueue",
                3000,
                messages -> {

                    // 1、filter fail message-list
                    List<MessageData> validMessageTotal = messages.stream()
                            .filter(consumeRequest -> CollectionTool.isNotEmpty(consumeRequest.getMessageList()))
                            .flatMap(consumeRequest -> consumeRequest.getMessageList().stream())
                            .filter(messageData -> messageData.getId()>0 && messageData.getStatus()>0)
                            .collect(Collectors.toList());
                    if (CollectionTool.isEmpty(validMessageTotal)) {
                        return;
                    }

                    // 2、batch write consume-result, collect fail-message
                    List<Integer> failStatusList = Arrays.asList(MessageStatusEnum.EXECUTE_FAIL.getValue(), MessageStatusEnum.EXECUTE_TIMEOUT.getValue());
                    List<Long> failMessageIdList = new ArrayList<>();
                    for (List<MessageData> validMessageBatch : CollectionTool.split(validMessageTotal, 50)) {
                        List<Message> messageList = new ArrayList<>();
                        for (MessageData messageData : validMessageBatch) {

                            // adaptor
                            Message message = new Message();
                            message.setId(messageData.getId());
                            message.setStatus(messageData.getStatus());
                            //message.setTopic(messageData.getTopic());
                            message.setConsumeLog( ConsumeLogUtil.HR_TAG +  ConsumeLogUtil.generateConsumeLog("消费消息", messageData.getConsumeLog() ));
                            message.setConsumeInstanceUuid(messageData.getConsumeInstanceUuid());

                            // collect
                            messageList.add(message);

                            // fail-retry collect
                            if (failStatusList.contains(messageData.getStatus())) {
                                failMessageIdList.add(message.getId());
                            }
                        }
                        brokerBootstrap.getMessageMapper().batchUpdateStatus(messageList);
                    }

                    /**
                     * 3、fail retry
                     */
                    if (CollectionTool.isNotEmpty(failMessageIdList)) {
                        // query fail
                        List<Message> failMessageList = brokerBootstrap.getMessageMapper().queryRetryDataById(failMessageIdList, failStatusList);
                        // fail retry
                        failRetry(failMessageList, failStatusList);
                    }

                },
                30,
                50);

    }

    public void stop(){
        // do nothing
    }


    // ---------------------- tool ----------------------

    /**
     * fail retry
     *
     * @param failMessageList
     */
    public void failRetry(List<Message> failMessageList, List<Integer> failStatusList) {
        if (CollectionTool.isEmpty(failMessageList)) {
            return;
        }

        // fill retry info
        for (Message message : failMessageList) {

            // valid
            Topic topic = brokerBootstrap.getLocalCacheThreadHelper().findTopic(message.getTopic());
            if (topic == null) {
                topic = new Topic();
                topic.setRetryInterval(3);
                topic.setRetryCount(3);
                topic.setRetryStrategy(RetryStrategyEnum.FIXED_RETREAT.getValue());
            }
            int totalRetryCount = Math.max(topic.getRetryCount(), message.getRetryCountRemain());

            // calculate retry info
            RetryStrategyEnum retryStrategyEnum = RetryStrategyEnum.match(topic.getRetryStrategy(), RetryStrategyEnum.FIXED_RETREAT);
            long delaySecond = 0;
            if (retryStrategyEnum == RetryStrategyEnum.FIXED_RETREAT) {
                delaySecond = topic.getRetryInterval();
            } else if (retryStrategyEnum == RetryStrategyEnum.LINEAR_RETREAT) {
                delaySecond = (long) topic.getRetryInterval() * (totalRetryCount - message.getRetryCountRemain() + 1);
            } else if (retryStrategyEnum == RetryStrategyEnum.RANDOM_RETREAT) {
                delaySecond = ThreadLocalRandom.current().nextInt(topic.getRetryInterval());
            }
            Date newEffectTime = DateTool.addSeconds(new Date(), delaySecond);

            // write retry data
            //message.setRetryCountRemain( message.getRetryCountRemain()-1 );
            //message.setStatus(MessageStatusEnum.NEW.getValue());
            //message.setConsumeLog(ConsumeLogUtil.appendConsumeLog(message.getConsumeLog(), "失败重试", null));
            message.setEffectTime(newEffectTime);
        }

        // write retry
        brokerBootstrap.getMessageMapper().batchFailRetry(failMessageList,
                failStatusList,
                MessageStatusEnum.NEW.getValue(),
                ConsumeLogUtil.HR_TAG + ConsumeLogUtil.generateConsumeLog("失败重试", "message status change to：" + MessageStatusEnum.NEW)
        );
    }


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
     * pull and lock message
     *
     * @param pullRequest
     * @return
     */
    public Response<List<MessageData>> pullAndLock(PullRequest pullRequest) {

        // valid
        if (CollectionTool.isEmpty(pullRequest.getTopicList())
                || StringTool.isBlank(pullRequest.getAppname())
                || StringTool.isBlank(pullRequest.getInstanceUuid())) {
            return Response.of(401, "Illegal parameters.");
        }

        // filter topic
        List<String> validTopicList = new ArrayList<>();
        for (String topic: pullRequest.getTopicList()) {
            Topic topicData = brokerBootstrap.getLocalCacheThreadHelper().findTopic(topic);
            if (topicData!=null && topicData.getStatus() == TopicStatusEnum.INACTIVE.getValue()) {
                // inactive topic, pass
                continue;
            }
            validTopicList.add(topic);
        }
        if (CollectionTool.isEmpty(validTopicList)) {
            return Response.of(402, "Illegal parameter, not valid topic.");
        }

        // match partition
        PartitionUtil.PartitionRange partitionRange = brokerBootstrap.getLocalCacheThreadHelper().findPartitionRangeByAppnameAndUuid(pullRequest.getAppname(), pullRequest.getInstanceUuid());
        if (partitionRange == null) {
            return Response.of(402, "Current instanceUuid has not been assigned a partition.");
        }

        // 1、消息检索
        int pagesize = pullRequest.getBatchsize();
        Date prePullTime = DateTool.addSeconds(new Date(), 30);
        List<Message> messageList = brokerBootstrap.getMessageMapper().pullQuery(
                pullRequest.getTopicList(),
                MessageStatusEnum.NEW.getValue(),
                partitionRange.getPartitionIdFrom(),
                partitionRange.getPartitionIdTo(),
                prePullTime,
                pagesize);
        if (CollectionTool.isEmpty(messageList)) {
            return Response.ofSuccess();
        }

        /**
         * markAsProcessingOnFetch: 默认为 true；
         *      true：两阶段更新，消费前后均更新消息；
         *          1、流程：拉取时更新为 “RUNNING”，消费完成后，再根据结果更新状态。
         *              - pullQuery + pullLock (“RUNNING”)
         *              - remoting http
         *              - dispatch + consumer ( “SUCCESS”、“FAIL” )
         *          2、优点：避免重复消费：RUNNING，等同于锁定状态；
         *          3、缺点：
         *              - 状态管理复杂：需要维护额外的状态流转逻辑；
         *              - 可能出现丢消息：
         *                  - 如果拉消息网络异常：丢消息；
         *                  - 消费端宕机且未及时恢复：丢消息；“执行中”状态的消息可能会被挂起，需依赖超时机制来释放。       【解决方案：stuck-message 检测恢复机制】 >  标记失败( 消息丢失 / 默认机制 )  or 标记初始状态 ( 导致延迟 )
         *          4、适用场景：需要严格保证消息不被重复消费的业务场景；例如订单支付、库存扣减等。
         *      false：仅消费后更新；消费后更新为 “SUCCESS、FAIL”
         *          1、流程：拉取消息时不修改状态；消费完成后，再根据结果更新状态。
         *              - pullQuery
         *              - remoting http
         *              - dispatch + consumer ( “SUCCESS”、“FAIL” )
         *          2、优点：
         *              - 简单易实现：不需要复杂的中间状态管理。
         *              - 避免丢消息现象：支持并发消费：多个消费者可以同时处理不同消息，提高吞吐量。
         *          3、缺点：
         *              - 可能重复消费：
         *                  消息结果异步回调，客户端无法感知回调速度和结果；会重复拉取消息，重复消费；
         *                  如果消费失败但未通知消息系统，消息可能会被再次拉取。                         【解决方案：客户端幂等】借助消息 “msgId”，客户端进行幂等处理；
         *          4、适用场景：对消息处理效率要求较高，且能够容忍短暂重复消费的场景。
         */
        /*boolean markAsProcessingOnFetch = true;*/
        // 2、消息锁定, with uuid
        List<Long> messageIdList = messageList.stream().map(Message::getId).collect(Collectors.toList());
        int count = brokerBootstrap.getMessageMapper().pullLock(messageIdList, pullRequest.getInstanceUuid(), MessageStatusEnum.NEW.getValue(), MessageStatusEnum.RUNNING.getValue());

        // 3、锁定消息检索, with uuid （锁定失败，过滤锁定成功数据）
        if (count < messageList.size()) {
            messageList = brokerBootstrap.getMessageMapper().pullQueryByUuid(messageIdList, pullRequest.getInstanceUuid(), MessageStatusEnum.RUNNING.getValue());
            if (CollectionTool.isEmpty(messageList)) {
                // lock fail all
                return Response.ofSuccess();
            }
        }

        // adaptor
        List<MessageData> messageDataList = new ArrayList<>();
        for (Message message : messageList) {

            // find timeout
            Topic topicData = brokerBootstrap.getLocalCacheThreadHelper().findTopic(message.getTopic());
            Integer executionTimeout = topicData!=null?topicData.getExecutionTimeout():null;

            // collect
            messageDataList.add(new MessageData(
                    message.getId(),
                    message.getTopic(),
                    message.getData(),
                    message.getEffectTime().getTime(),
                    executionTimeout));
        }
        return Response.ofSuccess(messageDataList);
    }

    /**
     * pull pre-check
     */
    public Response<String> pullPreCheck(PullRequest pullRequest) {

        // valid
        if (StringTool.isBlank(pullRequest.getAppname()) || StringTool.isBlank(pullRequest.getInstanceUuid())) {
            return Response.of(401, "Illegal parameters.");
        }

        // match partition
        PartitionUtil.PartitionRange partitionRange = brokerBootstrap.getLocalCacheThreadHelper().findPartitionRangeByAppnameAndUuid(pullRequest.getAppname(), pullRequest.getInstanceUuid());
        if (partitionRange == null) {
            return Response.of(402, "Current instanceUuid has not been assigned a partition.");
        }

        return Response.ofSuccess(partitionRange.getPartitionIdFrom() + "-" + partitionRange.getPartitionIdTo());
    }

}
