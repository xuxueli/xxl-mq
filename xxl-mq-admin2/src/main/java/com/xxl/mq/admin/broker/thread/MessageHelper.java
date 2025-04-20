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
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.concurrent.MessageQueue;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.response.Response;

import java.util.*;
import java.util.stream.Collectors;

import static com.xxl.mq.admin.broker.thread.AccessTokenThreadHelper.BEAT_TIME_INTERVAL;

/**
 * prude message queue helper
 *
 * @author xuxueli
 */
public class MessageHelper {

    // ---------------------- init ----------------------

    private final BrokerFactory brokerFactory;
    public MessageHelper(BrokerFactory brokerFactory) {
        this.brokerFactory = brokerFactory;
    }

    // ---------------------- start / stop ----------------------

    private volatile MessageQueue<ProduceRequest> produceMessageQueue;
    private volatile MessageQueue<ConsumeRequest> consumeMessageQueue;
    private volatile CyclicThread failMessageProcessThread;

    /**
     * start
     *
     * remark：
     *      1、Produce MessageQueue
     *      2、Consume MessageQueue
     *      3、FailMessage Process
     */
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
                            Topic topicData = brokerFactory.getLocalCacheThreadHelper().findTopic(messageData.getTopic());
                            PartitionRouteStrategyEnum partitionRouteStrategyEnum = PartitionRouteStrategyEnum.match(topicData!=null?topicData.getPartitionStrategy():-1, PartitionRouteStrategyEnum.HASH);
                            Map<String, PartitionUtil.PartitionRange> instancePartitionRange = brokerFactory.getLocalCacheThreadHelper().findPartitionRangeByTopic(messageData.getTopic());

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
                    }
                },
                50,
                50);

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

                    /**
                     * 3、fail retry
                     *      fail：status fail or timeout-fail
                     *      process:    TODO , fail retry
                     *          retryCount-1
                     *          status：rollback 2 NEW
                     *          effectTime
                     *              long addSecond = 0;
                     *              if (retryStrategyEnum == RetryStrategyEnum.FIXED_RETREAT) {
                     *                  addSecond = retryInterval;
                     *              } else if (retryStrategyEnum == RetryStrategyEnum.LINEAR_RETREAT) {
                     *                  addSecond = retryInterval * (retryCount-retryCount_remain + 1);
                     *              } else if (retryStrategyEnum == RetryStrategyEnum.EXPONENTIAL_RETREAT) {
                     *                  addSecond = retryInterval * (int)Math.pow(2, (retryCount-retryCount_remain + 1));
                     *              }
                     */
                    if (CollectionTool.isNotEmpty(failMessageList)) {
                        for (Message message : failMessageList) {

                            // match topic
                            Topic topic = brokerFactory.getLocalCacheThreadHelper().findTopic(message.getTopic());
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

                            // write retry data
                            //message.setRetryCount( -1 );      // RetryCount change；
                            message.setStatus(MessageStatusEnum.NEW.getValue());
                            message.setEffectTime(newEffectTime);

                        }

                        // fail retry
                        //brokerFactory.getMessageMapper().batchFailRetry(failMessageList);
                    }

                },
                30,
                50);

        failMessageProcessThread = new CyclicThread("failMessageProcessThread", true, new Runnable() {
            @Override
            public void run() {

                /**
                 * TODO, stuck process, 3s查询一次，直到查询不到结束；
                 *
                 * 1、find error message
                 *      fail: fail-status && updatetime > 5s(avoid compete)
                 *      stuck：running-status && updatetime > 5min （默认超时时间）
                  */
                List<Message> stuckMessageList = null;

                // 2、fail-message process，retry

                // 3、stuck-message process，mark fail and fail-retry

            }
        }, 60 * 1000, true);
        failMessageProcessThread.start();

    }

    public void stop(){
        // do nothing
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
        PartitionUtil.PartitionRange partitionRange = brokerFactory.getLocalCacheThreadHelper().findPartitionRangeByAppnameAndUuid(pullRequest.getAppname(), pullRequest.getInstanceUuid());
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
