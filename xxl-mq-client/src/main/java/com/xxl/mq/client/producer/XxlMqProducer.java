package com.xxl.mq.client.producer;

import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.rpc.util.IpUtil;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqProducer {

    // ---------------------- produce message ----------------------

    /**
     * create message
     *
     * @param mqMessage
     * @return
     */
    public static void validMessage(XxlMqMessage mqMessage){

        // topic
        if (mqMessage.getTopic()==null || mqMessage.getTopic().trim().length()==0 || mqMessage.getTopic().length()>512) {
            throw new IllegalArgumentException("xxl-mq, topic invalid.");
        }

        // group
        if (mqMessage.getTopic()==null || mqMessage.getTopic().trim().length()==0) {
            mqMessage.setTopic(ConsumerRegistryHelper.DEFAULT_GROUP);
        }
        if (mqMessage.getTopic().length() > 256) {
            throw new IllegalArgumentException("xxl-mq, group invalid.");
        }

        // data
        if (mqMessage.getData() == null) {
            mqMessage.setData("");
        }

        // status
        mqMessage.setStatus(XxlMqMessageStatus.NEW.name());


        // retryCount
        if (mqMessage.getRetryCount() < 0) {
            mqMessage.setRetryCount(0);
        }
        // shardingId
        if (mqMessage.getShardingId() < 0) {
            mqMessage.setShardingId(0);
        }

        // delayTime
        if (mqMessage.getEffectTime() == null) {
            mqMessage.setEffectTime(new Date());
        }

        // timeout
        if (mqMessage.getTimeout() < 0) {
            mqMessage.setTimeout(0);
        }

        // log
        String appendLog = MessageFormat.format("<hr>操作: 消息新增<br>》》》消息生产者: {0}", IpUtil.getIp());
        mqMessage.setLog(appendLog);
    }

    /**
     * produce message
     */
    public static void produce(String topic, String group, String data, int retryCount, long shardingId, Date effectTime){

        // make
        XxlMqMessage mqMessage = new XxlMqMessage();
        mqMessage.setTopic(topic);
        mqMessage.setGroup(group);
        mqMessage.setData(data);
        mqMessage.setRetryCount(retryCount);
        mqMessage.setShardingId(shardingId);
        mqMessage.setEffectTime(effectTime);

        // valid
        validMessage(mqMessage);

        // send
        XxlMqClientFactory.getXxlMqBroker().addMessages(Arrays.asList(mqMessage));

        // TODO，多线程异步发送
    }

    public static void produce(String topic, String data){
        produce(topic, null, data, 0, 0, null);
    }

    // ---------------------- broadcast message ----------------------

    /**
     * broadcast produce
     */
    public static void broadcast(String topic, String data){
        Set<String> groupList = ConsumerRegistryHelper.getTotalGroupList(topic);
        for (String group: groupList) {
            produce(topic, group, data, 0, 0, null);
        }
    }

}
