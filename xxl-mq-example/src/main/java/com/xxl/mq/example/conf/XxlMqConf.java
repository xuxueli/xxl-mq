package com.xxl.mq.example.conf;

import com.xxl.mq.client.factory.XxlMqClientFactory;
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
    public XxlMqClientFactory getXxlMqConsumer(){
        XxlMqClientFactory xxlMqClientFactory = new XxlMqClientFactory();
        xxlMqClientFactory.setZkaddress(zkaddress);
        xxlMqClientFactory.setZkdigest(zkdigest);
        xxlMqClientFactory.setEnv(env);

        return xxlMqClientFactory;
    }

}
