package com.xxl.mq.client;

import com.xxl.mq.client.rpc.netcom.NetComClientProxy;
import com.xxl.mq.client.service.XxlMqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 16/8/30.
 */
public class XxlMqClient {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqClient.class);

    private static XxlMqService xxlMqService;
    public static XxlMqService getXxlMqService() {
        if (xxlMqService!=null) {
            return xxlMqService;
        }
        try {
            xxlMqService = (XxlMqService) new NetComClientProxy(XxlMqService.class, 1000 * 5, null).getObject();
        } catch (Exception e) {
            logger.error("", e);
        }
        return xxlMqService;
    }

}
