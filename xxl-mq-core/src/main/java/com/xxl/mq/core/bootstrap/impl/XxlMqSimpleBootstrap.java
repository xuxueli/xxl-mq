package com.xxl.mq.core.bootstrap.impl;

import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.consumer.annotation.XxlMq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * xxl-mq spring bootstrap
 *
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqSimpleBootstrap extends XxlMqBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(XxlMqSimpleBootstrap.class);


    private List<Object> xxlMqBeanList = new ArrayList<>();
    public List<Object> getXxlMqBeanList() {
        return xxlMqBeanList;
    }
    public void setXxlMqBeanList(List<Object> xxlMqBeanList) {
        this.xxlMqBeanList = xxlMqBeanList;
    }

    @Override
    public void start() {

        // init MqConsumer
        initMqConsumer(xxlMqBeanList);

        // super start
        try {
            super.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        super.stop();
    }

    /**
     * init MqConsumer form method
     *
     * @param xxlMqBeanList
     */
    public void initMqConsumer(List<Object> xxlMqBeanList) {
        if (xxlMqBeanList==null || xxlMqBeanList.isEmpty()) {
            return;
        }

        // init mq consumer from method
        for (Object bean: xxlMqBeanList) {
            // method
            Method[] methods = bean.getClass().getDeclaredMethods();
            if (methods.length == 0) {
                continue;
            }
            for (Method executeMethod : methods) {
                XxlMq xxlMq = executeMethod.getAnnotation(XxlMq.class);
                // registry
                registryMethodConsumer(xxlMq, bean, executeMethod);
            }

        }

    }

}
