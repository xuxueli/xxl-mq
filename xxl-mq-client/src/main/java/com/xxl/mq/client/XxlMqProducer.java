package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqProducer {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqProducer.class);

    public static void produce(Message message){
        try {
            XxlMqClient.getXxlMqService().saveMessage(message);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
