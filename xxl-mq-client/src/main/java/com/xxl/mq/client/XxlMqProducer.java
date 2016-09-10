package com.xxl.mq.client;

import com.xxl.mq.client.consumer.remote.XxlMqClient;
import com.xxl.mq.client.message.MessageStatus;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.rpc.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqProducer {
    private static Logger logger = LoggerFactory.getLogger(XxlMqProducer.class);

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
                logger.warn(">>>>>>>>>>> xxl-mq, message data length over limit 2048");
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

        XxlMqClient.getXxlMqService().saveMessage(message);
    }

    public static void produce(String name, Map<String, String> dataMap){
        produce(name, dataMap, null, 0);
    }

}
