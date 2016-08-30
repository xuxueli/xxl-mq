package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.netcom.NetComClientProxy;
import com.xxl.mq.client.service.XxlMqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqProducer {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqProducer.class);

    private static XxlMqService brokerService;
    public static XxlMqService getBrokerService() {
        if (brokerService!=null) {
            return brokerService;
        }
        try {
            brokerService = (XxlMqService) new NetComClientProxy(XxlMqService.class, 1000 * 5, null).getObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return brokerService;
    }

    public static void saveMessage(Message message){
        try {
            getBrokerService().saveMessage(message);
        } catch (Exception e) {
            logger.error("", e);
        }
    }

}
