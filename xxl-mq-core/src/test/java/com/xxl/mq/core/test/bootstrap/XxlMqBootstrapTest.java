package com.xxl.mq.core.test.bootstrap;

import com.xxl.mq.core.XxlMqHelper;
import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.consumer.IConsumer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class XxlMqBootstrapTest {
    private static final Logger logger = LoggerFactory.getLogger(XxlMqBootstrapTest.class);

    private static String url = "http://127.0.0.1:8080/xxl-mq-admin/openapi";
    private static String service = "brokerService";
    private static String accessToken = "defaultaccesstoken";

    @Test
    public void test() throws InterruptedException {

        XxlMqBootstrap xxlMqBootstrap = new XxlMqBootstrap();
        xxlMqBootstrap.setAddress(url);
        xxlMqBootstrap.setAccesstoken(accessToken);
        xxlMqBootstrap.setAppname("xxl-mq-sample");

        // consumer
        xxlMqBootstrap.registryConsumer("topic_sample", new IConsumer() {
            @Override
            public void consume() throws Exception {
                logger.info("consume message:" + XxlMqHelper.getMessageData());
                XxlMqHelper.consumeSuccess();
            }
        });

        xxlMqBootstrap.start();

        TimeUnit.SECONDS.sleep(10 * 60);
    }

    @Test
    public void test2() throws InterruptedException {
        XxlMqBootstrap xxlMqBootstrap = new XxlMqBootstrap();
        xxlMqBootstrap.setAddress(url);
        xxlMqBootstrap.setAccesstoken(accessToken);
        xxlMqBootstrap.setAppname("xxl-mq-sample");

        xxlMqBootstrap.start();

        // produce
        for (int i = 0; i < 10; i++) {
            XxlMqHelper.produce("topic_sample", "data-" + i);
        }

        TimeUnit.SECONDS.sleep(10);
    }

}
