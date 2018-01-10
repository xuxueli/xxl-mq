package com.xxl.mq.client.consumer.remote;

import com.xxl.mq.client.broker.remote.IXxlMqBroker;
import com.xxl.mq.client.rpc.netcom.NetComClientProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xuxueli on 16/8/30.
 */
public class XxlMqClient {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqClient.class);

    private static IXxlMqBroker xxlMqService;
    private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);

    public static IXxlMqBroker getXxlMqService() {
        if (xxlMqService != null) {
            return xxlMqService;
        }

        /*CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.countDown();
        countDownLatch.await();*/

        try {
            if (INSTANCE_INIT_LOCK.tryLock(3, TimeUnit.SECONDS)) {
                try {
                    xxlMqService = (IXxlMqBroker) new NetComClientProxy(IXxlMqBroker.class, 1000 * 5, null).getObject();
                    logger.warn(">>>>>>>>>>> xxl-mq, XxlMqClient.xxlMqService init success.");
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    INSTANCE_INIT_LOCK.unlock();
                }
            }
        } catch (InterruptedException e) {
            logger.warn(">>>>>>>>>>> xxl-mq, XxlMqClient.xxlMqService init error.", e);
            logger.error(e.getMessage(), e);
        }

        if (xxlMqService == null) {
            throw new NullPointerException(">>>>>>>>>>> xxl-mq, XxlMqClient.xxlMqService is null.");
        }

        return xxlMqService;
    }

}
