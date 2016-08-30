package com.xxl.mq.client;

import com.xxl.mq.client.rpc.netcom.NetComClientProxy;
import com.xxl.mq.client.service.XxlMqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

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
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            xxlMqService = (XxlMqService) new NetComClientProxy(XxlMqService.class, 1000 * 5, null).getObject();
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            countDownLatch.countDown();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("", e);
        }
        return xxlMqService;
    }

}
