package com.xxl.mq.client.factory.impl;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author xuxueli 2018-11-18 21:18:10
 */
public class XxlMqSpringClientFactory implements ApplicationContextAware, DisposableBean {

    // ---------------------- param  ----------------------

    private String zkaddress;
    private String zkdigest;
    private String env;

    public void setZkaddress(String zkaddress) {
        this.zkaddress = zkaddress;
    }
    public void setZkdigest(String zkdigest) {
        this.zkdigest = zkdigest;
    }
    public void setEnv(String env) {
        this.env = env;
    }

    // XxlMqClientFactory
    private XxlMqClientFactory xxlMqClientFactory;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // load consumer from spring
        List<IMqConsumer> consumerList = new ArrayList<>();

        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(MqConsumer.class);
        if (serviceMap!=null && serviceMap.size()>0) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof IMqConsumer) {
                    consumerList.add((IMqConsumer) serviceBean);
                }
            }
        }

        // init
        xxlMqClientFactory = new XxlMqClientFactory();

        xxlMqClientFactory.setZkaddress(zkaddress);
        xxlMqClientFactory.setZkdigest(zkdigest);
        xxlMqClientFactory.setEnv(env);
        xxlMqClientFactory.setConsumerList(consumerList);

        xxlMqClientFactory.init();
    }

    @Override
    public void destroy() throws Exception {
        xxlMqClientFactory.destroy();
    }

}
