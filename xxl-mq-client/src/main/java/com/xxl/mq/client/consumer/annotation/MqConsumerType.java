package com.xxl.mq.client.consumer.annotation;

/**
 * Created by xuxueli on 16/9/10.
 */
public enum MqConsumerType {
    /**
     * 广播消息 : 发布/订阅模式, 一条消息将会广播发送给所有在线的Consumer
     */
    TOPIC,
    /**
     * 串行队列 : 点对点模式, 消息进去队列之后, 只会被消费一次。同一Topic下的多个Consumer并行消费消息, 吞吐量较大
     */
    SERIAL_QUEUE,
    /**
     * 并行队列 : 点对点模式, 消息进去队列之后, 只会被消费一次。但是,同一个Topic下只会有一个Consumer串行消费消息, 适用于严格限制并发的场景
     */
    QUEUE
}
