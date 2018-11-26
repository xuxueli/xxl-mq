package com.xxl.mq.sample.frameless.conf;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;

public class XxlMqConf {
    private static Logger logger = LoggerFactory.getLogger(XxlMqConf.class);


    private static XxlMqConf instance = new XxlMqConf();
    public static XxlMqConf getInstance() {
        return instance;
    }


    // XxlMqClientFactory
    private XxlMqClientFactory xxlMqClientFactory;

    /**
     * start
     */
    public void start(List<IMqConsumer> consumerList){

        if (consumerList==null || consumerList.size()==0) {
            return;
        }

        // load executor prop
        Properties xxlJobProp = loadProperties("xxl-mq.properties");

        xxlMqClientFactory = new XxlMqClientFactory();
        xxlMqClientFactory.setAdminAddress(xxlJobProp.getProperty("xxl.mq.admin.address"));
        xxlMqClientFactory.setConsumerList(consumerList);

        xxlMqClientFactory.init();

    }

    /**
     * stop
     */
    public void stop() throws Exception {
        if (xxlMqClientFactory != null) {
            xxlMqClientFactory.destroy();
        }
    }


    public static Properties loadProperties(String propertyFileName) {
        InputStreamReader in = null;
        try {
            ClassLoader loder = Thread.currentThread().getContextClassLoader();

            in = new InputStreamReader(loder.getResourceAsStream(propertyFileName), "UTF-8");;
            if (in != null) {
                Properties prop = new Properties();
                prop.load(in);
                return prop;
            }
        } catch (IOException e) {
            logger.error("load {} error!", propertyFileName);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("close {} error!", propertyFileName);
                }
            }
        }
        return null;
    }

}
