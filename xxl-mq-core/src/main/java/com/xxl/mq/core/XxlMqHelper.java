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


    // --------------------------------- get message data (for consumer) ---------------------------------

    /**
     * get message data
     *
     * @return
     */
    public static long getMessageId() {
        return XxlMqContext.getContext().getId();
    }

    /**
     * get message data
     *
     * @return
     */
    public static String getMessageData() {
        return XxlMqContext.getContext().getData();
    }


    // --------------------------------- set consume result (for consumer) ---------------------------------

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


    // --------------------------------- produce message (for producer) ---------------------------------

    /**
     * produce message
     *
     * @param topic
     * @param data
     * @param effectTime
     * @param bizId
     * @return
     */
    public static boolean produce(String topic, String data, long effectTime, long bizId) {
        // send message
        MessageData messageData = new MessageData(topic, data, bizId, effectTime);
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
        return produce(topic, data, effectTime, 0);
    }

    /**
     * produce message
     *
     * @param topic
     * @param data
     * @param bizId
     * @return
     */
    public static boolean produce2(String topic, String data, long bizId) {
        return produce(topic, data, -1, bizId);
    }

    /**
     * produce message
     *
     * @param topic
     * @param data
     * @return
     */
    public static boolean produce(String topic, String data) {
        return produce(topic, data, -1, 0);
    }

}
