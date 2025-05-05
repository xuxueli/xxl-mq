package com.xxl.mq.core;

import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.context.XxlMqContext;
import com.xxl.mq.core.openapi.model.MessageData;

/**
 * xxl-mq helper
 *
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqHelper {


    // --------------------------------- consume ---------------------------------

    /**
     * get message data
     *
     * @return
     */
    public static String getMessageData() {
        return XxlMqContext.getContext().getData();
    }

    /**
     * consume success
     */
    public static void consumeSuccess() {
        XxlMqContext.getContext().setStatus(2);
    }

    /**
     * consume success with log
     */
    public static void consumeSuccess(String consumeLog) {
        XxlMqContext.getContext().setStatus(2);
        XxlMqContext.getContext().setConsumeLog(consumeLog);
    }

    /**
     * consume fail
     */
    public static void consumeFail() {
        XxlMqContext.getContext().setStatus(3);
    }

    /**
     * consume fail with log
     */
    public static void consumeFail(String consumeLog) {
        XxlMqContext.getContext().setStatus(3);
        XxlMqContext.getContext().setConsumeLog(consumeLog);
    }

    /**
     * consume timeout
     */
    public static void consumeTimeout() {
        XxlMqContext.getContext().setStatus(4);
    }

    /**
     * consume timeout with log
     */
    public static void consumeTimeout(String consumeLog) {
        XxlMqContext.getContext().setStatus(4);
        XxlMqContext.getContext().setConsumeLog(consumeLog);
    }

    // --------------------------------- produce ---------------------------------

    /**
     * produce message
     *
     * @param topic
     * @param data
     * @param effectTime
     * @param partitionKey
     * @return
     */
    public static boolean produce(String topic, String data, long effectTime, String partitionKey) {

        MessageData messageData = new MessageData();
        messageData.setTopic(topic);
        messageData.setPartitionKey(partitionKey);
        messageData.setData(data);
        messageData.setEffectTime(effectTime);

        return XxlMqBootstrap.getInstance().getMessageThread().produceSend(messageData);
    }

    /**
     * produce message
     *
     * @param topic
     * @param data
     * @param effectTime
     * @return
     */
    public static boolean produce(String topic, String data, long effectTime) {
        return produce(topic, data, effectTime, null);
    }

    /**
     * produce message
     *
     * @param topic
     * @param data
     * @return
     */
    public static boolean produce(String topic, String data) {
        return produce(topic, data, System.currentTimeMillis(), null);
    }

}
