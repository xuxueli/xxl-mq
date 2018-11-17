package com.xxl.mq.client.producer;

import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.xxl.mq.client.util.JacksonUtil;
import com.xxl.rpc.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqProducer {
    private static Logger logger = LoggerFactory.getLogger(XxlMqProducer.class);

    // ---------------------- for queue message ----------------------

    /**
     * 生产消息: 该方法生产的消息支持以下两种方式消费, 消费方式通过Consumer配置确定
     *      QUEUE = 点对点模式, 消息进去队列之后, 只会被消费一次。但是,同一个Topic下只会有一个Consumer串行消费消息, 适用于严格限制并发的场景
     *      SERIAL_QUEUE = 串行队列 : 点对点模式, 消息进去队列之后, 只会被消费一次。同一Topic下的多个Consumer并行消费消息, 吞吐量较大
     * @param topic          消息主题
     * @param dataMap       消息数据
     * @param effectTime     延迟执行时间, 默认为new Date(), 即立即执行
     * @param retryCount    失败重试次数, 默认0, 即失败不重试
     */
    public static void produce(String topic, String group, Map<String, String> dataMap, int retryCount, long shardingId, Date effectTime){

        // topic
        if (topic==null || topic.trim().length()==0 || topic.length()>255) {
            throw new IllegalArgumentException("xxl-mq, topic invalid.");
        }
        if (group.length() > 512) {
            throw new IllegalArgumentException("xxl-mq, topic invalid.");
        }

        // group
        if (group==null || group.trim().length()==0) {
            group = ConsumerRegistryHelper.DEFAULT_GROUP;
        }
        if (group.length() > 256) {
            throw new IllegalArgumentException("xxl-mq, group invalid.");
        }

        // dataMap
        String dataJson = "";
        if (dataMap != null) {
            dataJson = JacksonUtil.writeValueAsString(dataMap);
        }

        // retryCount
        if (retryCount < 0) {
            retryCount = 0;
        }
        if (shardingId < 0) {
            shardingId = 0;
        }

        // delayTime
        if (effectTime==null) {
            effectTime = new Date();
        }

        // log
        String appendLog = MessageFormat.format("<hr>操作: 消息新增<br>》》》消息生产者: {0}", IpUtil.getIp());

        // package message
        XxlMqMessage message = new XxlMqMessage();
        message.setTopic(topic);
        message.setGroup(group);
        message.setData(dataJson);
        message.setStatus(XxlMqMessageStatus.NEW.name());
        message.setRetryCount(retryCount);
        message.setShardingId(shardingId);
        message.setEffectTime(effectTime);
        message.setLog(appendLog);

        XxlMqClientFactory.getXxlMqBroker().addMessages(Arrays.asList(message));
    }

    /**
     * 生产消息: 该方法生产的消息支持以下两种方式消费, 消费方式通过Consumer配置确定
     *      QUEUE = 点对点模式, 消息进去队列之后, 只会被消费一次。但是,同一个Topic下只会有一个Consumer串行消费消息, 适用于严格限制并发的场景
     *      SERIAL_QUEUE = 串行队列 : 点对点模式, 消息进去队列之后, 只会被消费一次。同一Topic下的多个Consumer并行消费消息, 吞吐量较大
     *
     * @param topic      消息主题
     * @param dataMap   消息数据
     */
    public static void produce(String topic, Map<String, String> dataMap){
        produce(topic, null, dataMap, 0, 0, null);
    }

    // ---------------------- for topic message ----------------------

    /**
     * 生产消息: TOPIC = 广播消息 : 发布/订阅模式, 一条消息将会广播发送给所有在线的Consumer
     * @param name
     * @param dataMap
     */
    public static void broadcast(String name, Map<String, String> dataMap){
        // name
        if (name==null || name.length()>255) {
            throw  new IllegalArgumentException("消息标题长度不合法");
        }
        // data
        String dataJson = "";
        if (dataMap!=null) {
            dataJson = JacksonUtil.writeValueAsString(dataMap);
            if (dataJson.length()>2048) {
                throw new IllegalArgumentException(">>>>>>>>>>> xxl-mq, message data length over limit 2048");
            }
        }

        Set<String> groupList = ConsumerRegistryHelper.getTotalGroupList(name);
        for (String group: groupList) {
            produce(name, dataMap);
        }

    }

}
