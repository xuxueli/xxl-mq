package com.xxl.mq.broker.conf;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job config
 *
 * @author xuxueli 2017-04-28
 */
@Configuration
public class XxlMqBrokerConfig implements InitializingBean{
    private static XxlMqBrokerConfig brokerConfig = null;
    public static XxlMqBrokerConfig getBrokerConfig() {
        return brokerConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        brokerConfig = this;
    }

    // conf
    @Value("${xxl.mq.login.username}")
    private String loginUsername;

    @Value("${xxl.mq.login.password}")
    private String loginPassword;


    public String getLoginUsername() {
        return loginUsername;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

}
