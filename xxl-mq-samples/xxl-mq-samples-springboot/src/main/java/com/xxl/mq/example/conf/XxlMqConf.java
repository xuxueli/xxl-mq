package com.xxl.mq.example.conf;

import com.xxl.mq.client.factory.impl.XxlMqSpringClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class XxlMqConf {

    // ---------------------- param ----------------------

    @Value("${xxl-mq.rpc.registry.zk.zkaddress}")
    private String zkaddress;

    @Value("${xxl-mq.rpc.registry.zk.zkdigest}")
    private String zkdigest;

    @Value("${xxl-mq.rpc.registry.zk.env}")
    private String env;

    @Bean
    public XxlMqSpringClientFactory getXxlMqConsumer(){
        XxlMqSpringClientFactory xxlMqSpringClientFactory = new XxlMqSpringClientFactory();
        xxlMqSpringClientFactory.setZkaddress(zkaddress);
        xxlMqSpringClientFactory.setZkdigest(zkdigest);
        xxlMqSpringClientFactory.setEnv(env);

        return xxlMqSpringClientFactory;
    }

}
