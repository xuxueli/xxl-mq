package com.xxl.mq.sample.springboot.conf;

import com.xxl.mq.core.bootstrap.impl.XxlMqSpringBootstrap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class XxlMqConfig {

    // ---------------------- param ----------------------

    @Value("${xxl.mq.admin.address}")
    private String address;
    @Value("${xxl.mq.admin.accesstoken}")
    private String accesstoken;
    @Value("${xxl.mq.client.appname}")
    private String appname;
    @Value("${xxl.mq.client.timeout}")
    private int timeout;
    @Value("${xxl.mq.client.consumer.enabled}")
    private Boolean consumerEnabled;
    @Value("${xxl.mq.client.consumer.pull.batchsize}")
    private int batchsize;
    @Value("${xxl.mq.client.consumer.pull.interval}")
    private int interval;
    @Value("${xxl.mq.client.consumer.threadPoolSize}")
    private int consumerThreadPoolSize;
    @Value("${xxl.mq.client.consumer.threadPoolMaxSize}")
    private int consumerThreadPoolMaxSize;

    @Bean
    public XxlMqSpringBootstrap getXxlMqConsumer() {
        // init xxl-mq spring bootstrap
        XxlMqSpringBootstrap xxlMqBootstrap = new XxlMqSpringBootstrap();
        xxlMqBootstrap.setAddress(address);
        xxlMqBootstrap.setAccesstoken(accesstoken);
        xxlMqBootstrap.setAppname(appname);
        xxlMqBootstrap.setTimeout(timeout);
        xxlMqBootstrap.setConsumerEnabled(consumerEnabled);
        xxlMqBootstrap.setPullBatchsize(batchsize);
        xxlMqBootstrap.setPullInterval(interval);
        xxlMqBootstrap.setConsumerThreadPoolSize(consumerThreadPoolSize);
        xxlMqBootstrap.setConsumerThreadPoolMaxSize(consumerThreadPoolMaxSize);
        return xxlMqBootstrap;
    }


}
