package com.xxl.mq.sample.frameless.conf;

import com.xxl.mq.core.bootstrap.impl.XxlMqSimpleBootstrap;
import com.xxl.mq.sample.frameless.mqconsumer.SimpleXxlMq;
import com.xxl.tool.core.PropTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;

public class FramelessXxlMqConfig {
    private static Logger logger = LoggerFactory.getLogger(FramelessXxlMqConfig.class);


    private static FramelessXxlMqConfig instance = new FramelessXxlMqConfig();
    public static FramelessXxlMqConfig getInstance() {
        return instance;
    }


    // XxlMqSimpleBootstrap
    private XxlMqSimpleBootstrap xxlMqBootstrap;

    /**
     * start
     */
    public void start(){

        // load prop
        Properties xxlMqProp = PropTool.loadProp("xxl-mq.properties");

        // init xxlMqBootstrap
        xxlMqBootstrap = new XxlMqSimpleBootstrap();
        xxlMqBootstrap.setAddress(xxlMqProp.getProperty("xxl.mq.admin.address"));
        xxlMqBootstrap.setAccesstoken(xxlMqProp.getProperty("xxl.mq.admin.accesstoken"));
        xxlMqBootstrap.setAppname(xxlMqProp.getProperty("xxl.mq.client.appname"));
        xxlMqBootstrap.setTimeout(PropTool.getInt(xxlMqProp, "xxl.mq.client.timeout"));
        xxlMqBootstrap.setConsumerEnabled(PropTool.getBoolean(xxlMqProp, "xxl.mq.client.consumer.enabled"));
        xxlMqBootstrap.setPullBatchsize(PropTool.getInt(xxlMqProp, "xxl.mq.client.consumer.pull.batchsize"));
        xxlMqBootstrap.setPullInterval(PropTool.getInt(xxlMqProp, "xxl.mq.client.consumer.pull.interval"));

        /**
         * set xxl-mq bean list, will scan consumer-method with @XxlMq
         */
        xxlMqBootstrap.setXxlMqBeanList(Arrays.asList(new SimpleXxlMq()));

        // start
        xxlMqBootstrap.start();
    }

    /**
     * stop
     */
    public void stop() throws Exception {
        if (xxlMqBootstrap != null) {
            xxlMqBootstrap.stop();
        }
    }


}
