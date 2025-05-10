package com.xxl.mq.sample.springboot.conf;

import com.xxl.mq.core.bootstrap.impl.XxlMqSpringBootstrap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class XxlMqConf {

    // ---------------------- param ----------------------

    @Value("${xxl.mq.admin.address}")
    private String address;
    @Value("${xxl.mq.admin.accesstoken}")
    private String accesstoken;
    @Value("${xxl.mq.client.appname}")
    private String appname;
    @Value("${xxl.mq.client.timeout}")
    private int timeout;
    @Value("${xxl.mq.client.pull.batchsize}")
    private int batchsize;
    @Value("${xxl.mq.client.pull.interval}")
    private int interval;


    @Bean
    public XxlMqSpringBootstrap getXxlMqConsumer(){
        // init xxl-mq spring bootstrap
        XxlMqSpringBootstrap xxlMqBootstrap = new XxlMqSpringBootstrap();
        xxlMqBootstrap.setAddress(address);
        xxlMqBootstrap.setAccesstoken(accesstoken);
        xxlMqBootstrap.setAppname(appname);
        xxlMqBootstrap.setTimeout(timeout);
        xxlMqBootstrap.setPullBatchsize(batchsize);
        xxlMqBootstrap.setPullInterval(interval);

        return xxlMqBootstrap;
    }


}
