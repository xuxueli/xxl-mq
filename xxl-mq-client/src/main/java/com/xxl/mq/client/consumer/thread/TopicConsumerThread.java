package com.xxl.mq.client.consumer.thread;

import com.xxl.mq.client.consumer.IMqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 16/9/10.
 */ // consumer thread
public class TopicConsumerThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(TopicConsumerThread.class);

    private IMqConsumer consumerHandler;

    public TopicConsumerThread(IMqConsumer consumerHandler) {
        this.consumerHandler = consumerHandler;
    }

    public IMqConsumer getConsumerHandler() {
        return consumerHandler;
    }

    @Override
    public void run() {

    }
}
