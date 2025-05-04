package com.xxl.mq.core.thread;

import com.xxl.mq.core.consumer.Consumer;
import com.xxl.mq.core.openapi.model.MessageData;

/**
 * consumer
 *
 * Created by xuxueli on 16/8/28.
 */
public class ConsumerRunable implements Runnable {

    private final MessageData message;
    private final Consumer consumer;

    public ConsumerRunable(MessageData message, Consumer consumer) {
        this.message = message;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        consumer.consume(message);
    }

}