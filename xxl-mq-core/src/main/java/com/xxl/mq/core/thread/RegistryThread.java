package com.xxl.mq.core.thread;

import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.openapi.model.RegistryRequest;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * registry thread
 *
 * Created by xuxueli on 16/8/28.
 */
public class RegistryThread {
    private static final Logger logger = LoggerFactory.getLogger(RegistryThread.class);

    /**
     * beat time interval
     */
    public static final int BEAT_TIME_INTERVAL = 30 * 1000;
    
    private final XxlMqBootstrap xxlMqBootstrap;

    public RegistryThread(final XxlMqBootstrap xxlMqBootstrap) {
        this.xxlMqBootstrap = xxlMqBootstrap;
    }

    /**
     * start
     */
    public void start() {
        CyclicThread cyclicRegistryThread = new CyclicThread("cyclicRegistryThread", true, new Runnable() {
            @Override
            public void run() {

                // param
                RegistryRequest registryRequest = new RegistryRequest();
                registryRequest.setAccesstoken(xxlMqBootstrap.getAccesstoken());
                registryRequest.setAppname(xxlMqBootstrap.getAppname());
                registryRequest.setInstanceUuid(xxlMqBootstrap.getInstanceUuid());
                registryRequest.setTopicList(xxlMqBootstrap.getAllConsumerTopicList());

                // retry 3 times
                for (int i = 0; i < 3; i++) {
                    try {
                        Response<String> registryResponse = xxlMqBootstrap.loadBrokerClient().registry(registryRequest);
                        if (registryResponse.isSuccess()) {
                            logger.debug(">>>>>>>>>>> xxl-mq RegistryThread-cyclicRegistryThread registry success, registryRequest:{}, registryResponse:{}", registryRequest, registryResponse);
                            break;
                        } else {
                            logger.error(">>>>>>>>>>> xxl-mq RegistryThread-cyclicRegistryThread registry fail, registryRequest:{}, registryResponse:{}", registryRequest, registryResponse);
                        }
                    } catch (Exception e) {
                        logger.error(">>>>>>>>>>> xxl-mq RegistryThread-cyclicRegistryThread registry error, registryRequest:{}", registryRequest, e);
                    }
                }
            }
        }, BEAT_TIME_INTERVAL, true);
        cyclicRegistryThread.start();
    }

    /**
     * stop
     */
    public void stop() {

        // param
        RegistryRequest registryRequest = new RegistryRequest();
        registryRequest.setAccesstoken(xxlMqBootstrap.getAccesstoken());
        registryRequest.setAppname(xxlMqBootstrap.getAppname());
        registryRequest.setInstanceUuid(xxlMqBootstrap.getInstanceUuid());
        registryRequest.setTopicList(xxlMqBootstrap.getAllConsumerTopicList());

        // retry 3 times
        for (int i = 0; i < 3; i++) {
            try {
                Response<String> registryResponse = xxlMqBootstrap.loadBrokerClient().registryRemove(registryRequest);
                if (registryResponse.isSuccess()) {
                    logger.debug(">>>>>>>>>>> xxl-mq RegistryThread-cyclicRegistryThread registry success, registryParam:{}", registryRequest);
                    break;
                }
            } catch (Exception e) {
                logger.error(">>>>>>>>>>> xxl-mq RegistryThread-cyclicRegistryThread registry error, registryParam:{}", registryRequest, e);
            }
        }
    }

}