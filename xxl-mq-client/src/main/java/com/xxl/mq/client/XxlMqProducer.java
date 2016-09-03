package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqProducer {

    public static void produce(Message message){
        XxlMqClient.getXxlMqService().saveMessage(message);
    }

}
