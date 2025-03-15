package com.xxl.mq.admin.openapi.registry.config;

import com.xxl.mq.admin.mapper.*;
import com.xxl.mq.admin.openapi.registry.thread.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * registry config
 *
 * @author xuxueli
 */
@Configuration
public class RegistryFactory implements InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(RegistryFactory.class);

    // ---------------------- instance ----------------------
    private static RegistryFactory instance;
    public static RegistryFactory getInstance() {
        return instance;
    }

    // ---------------------- helper ----------------------

    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private AccessTokenMapper accessTokenMapper;

    public ApplicationMapper getApplicationMapper() {
        return applicationMapper;
    }
    public AccessTokenMapper getAccessTokenMapper() {
        return accessTokenMapper;
    }

    // ---------------------- helper ----------------------

    /**
     * 5、AccessTokenHelpler
     */
    private AccessTokenHelpler accessTokenHelpler;

    public AccessTokenHelpler getAccessTokenHelpler() {
        return accessTokenHelpler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // base init
        instance = this;

        // 1、RegistryCacheHelpler
        // 5、AccessTokenHelpler
        try {
            accessTokenHelpler = new AccessTokenHelpler();
            accessTokenHelpler.start();
        } catch (Throwable e) {
            logger.error("RegistryFactory - accessTokenHelpler: start error", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        // 1、RegistryCacheHelpler
        // 5、AccessTokenHelpler
        try {
            accessTokenHelpler.stop();
        } catch (Throwable e) {
            logger.error("RegistryFactory - accessTokenHelpler: stop error", e);
        }

    }

}
