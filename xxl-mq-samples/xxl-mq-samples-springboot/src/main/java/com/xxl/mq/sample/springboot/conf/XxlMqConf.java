package com.xxl.mq.sample.springboot.conf;

import com.xxl.mq.client.factory.impl.XxlMqSpringClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class XxlMqConf {

    // ---------------------- param ----------------------

    @Value("${xxl.mq.admin.address}")
    private String adminAddress;
    @Value("${xxl.mq.accessToken}")
    private String accessToken;


    @Bean
    public XxlMqSpringClientFactory getXxlMqConsumer(){

        XxlMqSpringClientFactory xxlMqSpringClientFactory = new XxlMqSpringClientFactory();
        xxlMqSpringClientFactory.setAdminAddress(adminAddress);
        xxlMqSpringClientFactory.setAccessToken(accessToken);

        return xxlMqSpringClientFactory;
    }

}
