package com.xxl.mq.client.consumer.annotation;

/**
 * Created by xuxueli on 16/9/10.
 */
public enum MqConsumerType {
    /**
     * 广播消息
     */
    TOPIC,
    /**
     * 串行消费队列
     */
    SERIAL_QUEUE,
    /**
     * 并行消费队列
     */
    QUEUE
}
