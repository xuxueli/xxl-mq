package com.xxl.mq.client.consumer.thread;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.rpc.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

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

    // for message queue
    private LinkedBlockingQueue<XxlMqMessage> messageQueue = new LinkedBlockingQueue<XxlMqMessage>();
    public void pushMessage(XxlMqMessage msg){
        messageQueue.add(msg);
    }

    @Override
    public void run() {
        while (true) {
            try {
                XxlMqMessage msg = messageQueue.take();

                // consume message
                Map<String, String> data = null;
                if (msg.getData() != null && msg.getData().trim().length() > 0) {
                    data = JacksonUtil.readValue(msg.getData(), Map.class);
                }
                consumerHandler.consume(data);

            } catch (Exception e) {
                logger.error("", e);
            }

        }
    }
}
