package com.xxl.mq.broker.conf;

import com.xxl.mq.client.broker.XxlMqBroker;
import com.xxl.mq.client.broker.remote.IXxlMqMessageDao;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
public class XxlBrokerConf {


    @Value("${xxl-mq.broker.port}")
    private int port;
    @Value("${xxl-mq.broker.accessToken}")
    private String accessToken;


    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;

    @Bean
    public XxlMqBroker getXxlMqBroker(){
        XxlMqBroker xxlMqBroker = new XxlMqBroker();
        xxlMqBroker.setPort(port);
        xxlMqBroker.setXxlMqMessageDao(xxlMqMessageDao);

        return xxlMqBroker;
    }


}
