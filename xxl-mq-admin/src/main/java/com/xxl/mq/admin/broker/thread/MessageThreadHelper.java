package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerBootstrap;
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
import com.xxl.tool.concurrent.CyclicThread;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * prude message queue helper
 *
 * @author xuxueli
 */
public class MessageThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger(MessageThreadHelper.class);

    // ---------------------- init ----------------------

    private final BrokerBootstrap brokerBootstrap;
    public MessageThreadHelper(BrokerBootstrap brokerBootstrap) {
        this.brokerBootstrap = brokerBootstrap;
    }

    // ---------------------- start / stop ----------------------

    private volatile MessageQueue<ProduceRequest> produceMessageQueue;
    private volatile MessageQueue<ConsumeRequest> consumeMessageQueue;
    private volatile CyclicThread failMessageProcessThread;

    /**
     * start
     *
     * remark：
     *      1、Produce MessageQueue: batch write message to store
     *      2、Consume MessageQueue: 1、batch write consume result to store；2、find fail-message and retry.
     *      3、FailMessage Process: 1、find stuck message(running >5min), mark fail；2、find fail-message and retry.
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
                            message.setTopic(messageData.getTopic());
                            message.setConsumeLog( ConsumeLogUtil.HR_TAG +  ConsumeLogUtil.generateConsumeLog("消费消息", messageData.getConsumeLog() + "<br> Other：message status change to：" + MessageStatusEnum.match(messageData.getStatus(), null) ));

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

        failMessageProcessThread = new CyclicThread("failMessageProcessThread", true, new Runnable() {
            @Override
            public void run() {

                // 1、update stuck 2 fail, mark fail
                int updateStuck2FailCount = 0;
                int ret = brokerBootstrap.getMessageMapper().updateStuck2FailWithPage(
                        MessageStatusEnum.RUNNING.getValue(),
                        DateTool.addMinutes(new Date(), -5),
                        MessageStatusEnum.EXECUTE_TIMEOUT.getValue(),
                        500);
                while (ret > 0) {
                    updateStuck2FailCount += ret;
                    // avoid too fast
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    // next page
                    ret = brokerBootstrap.getMessageMapper().updateStuck2FailWithPage(
                            MessageStatusEnum.RUNNING.getValue(),
                            DateTool.addMinutes(new Date(), -5),
                            MessageStatusEnum.EXECUTE_TIMEOUT.getValue(),
                            50);
                }
                if (updateStuck2FailCount > 0) {
                    logger.info(">>>>>>>>>>> failMessageProcessThread, updateStuck2FailCount: {}", updateStuck2FailCount);
                }

                // 2、query fail-message, and retry
                int failRetryCount = 0;
                List<Integer> failStatusList = Arrays.asList(MessageStatusEnum.EXECUTE_FAIL.getValue(), MessageStatusEnum.EXECUTE_TIMEOUT.getValue());
                List<Message> retryFailData = brokerBootstrap.getMessageMapper().queryRetryDataByPage(failStatusList, 50);
                while (CollectionTool.isNotEmpty(retryFailData)) {
                    failRetryCount += retryFailData.size();
                    // do retry
                    failRetry(retryFailData, failStatusList);

                    // avoid too fast
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    // next page
                    retryFailData = brokerBootstrap.getMessageMapper().queryRetryDataByPage(failStatusList, 50);
                }
                if (failRetryCount > 0) {
                    logger.info(">>>>>>>>>>> failMessageProcessThread, failRetryCount: {}", failRetryCount);
                }

            }
        }, 60 * 1000, true);
        failMessageProcessThread.start();

    }

    public void stop(){
        // do nothing
    }

    /**
     * fail retry
     *
     * @param failMessageList
     */
    private void failRetry(List<Message> failMessageList, List<Integer> failStatusList) {
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
        PartitionUtil.PartitionRange partitionRange = brokerBootstrap.getLocalCacheThreadHelper().findPartitionRangeByAppnameAndUuid(pullRequest.getAppname(), pullRequest.getInstanceUuid());
        if (partitionRange == null) {
            return Response.of(402, "Current instanceUuid has not been assigned a partition.");
        }

        // 1、消息检索
        int pagesize = pullRequest.getBatchsize();
        List<Message> messageList = brokerBootstrap.getMessageMapper().pullQuery(pullRequest.getTopicList(), MessageStatusEnum.NEW.getValue(), partitionRange.getPartitionIdFrom(), partitionRange.getPartitionIdTo(), pagesize);
        if (CollectionTool.isEmpty(messageList)) {
            return Response.ofSuccess();
        }

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
            // collect
            messageDataList.add(new MessageData(
                    message.getId(),
                    message.getTopic(),
                    message.getData()));
        }
        return Response.ofSuccess(messageDataList);
    }

}
