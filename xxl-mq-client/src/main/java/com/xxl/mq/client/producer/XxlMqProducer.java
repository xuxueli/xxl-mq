package com.xxl.mq.client.producer;

import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.message.MessageStatus;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.topic.TopicHelper;
import com.xxl.mq.client.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * @param name          消息主题
     * @param dataMap       消息数据
     * @param delayTime     延迟执行时间, 默认为new Date(), 即立即执行
     * @param retryCount    失败重试次数, 默认0, 即失败不重试
     */
    public static void produce(String name, Map<String, String> dataMap, Date delayTime, int retryCount){

        // name
        if (name==null || name.length()>255) {
            throw  new IllegalArgumentException("消息标题长度不合法");
        }
        // data
        String dataJson = null;
        if (dataMap!=null) {
            dataJson = JacksonUtil.writeValueAsString(dataMap);
            if (dataJson.length()>2048) {
                throw new IllegalArgumentException(">>>>>>>>>>> xxl-mq, message data length over limit 2048");
            }
        }
        // delayTime
        if (delayTime==null) {
            delayTime = new Date();
        }
        // retryCount
        if (retryCount<0) {
            retryCount = 0;
        }

        // package message
        XxlMqMessage message = new XxlMqMessage();
        message.setName(name);
        message.setData(dataJson);
        message.setDelayTime(delayTime);
        message.setStatus(MessageStatus.NEW.name());
        message.setRetryCount(retryCount);

        XxlMqClientFactory.getXxlMqBroker().saveMessage(message);
    }

    /**
     * 生产消息: 该方法生产的消息支持以下两种方式消费, 消费方式通过Consumer配置确定
     *      QUEUE = 点对点模式, 消息进去队列之后, 只会被消费一次。但是,同一个Topic下只会有一个Consumer串行消费消息, 适用于严格限制并发的场景
     *      SERIAL_QUEUE = 串行队列 : 点对点模式, 消息进去队列之后, 只会被消费一次。同一Topic下的多个Consumer并行消费消息, 吞吐量较大
     *
     * @param name      消息主题
     * @param dataMap   消息数据
     */
    public static void produce(String name, Map<String, String> dataMap){
        produce(name, dataMap, null, 0);
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

        // TODO，废弃ZK广播，统一Queue方式；

        List<String> groupList = TopicHelper.getTotalGroupList(name);
        for (String group: groupList) {
            produce(name, dataMap);
        }

    }

}
