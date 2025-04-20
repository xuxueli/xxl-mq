package com.xxl.mq.admin.broker.config;

import com.xxl.mq.admin.broker.thread.AccessTokenThreadHelper;
import com.xxl.mq.admin.broker.thread.MessageHelper;
import com.xxl.mq.admin.broker.thread.LocalCacheThreadHelper;
import com.xxl.mq.admin.broker.thread.RegistryMessageQueueHelper;
import com.xxl.mq.admin.mapper.*;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.tool.jsonrpc.JsonRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;


/**
 * registry config
 *
 * @author xuxueli
 */
@Configuration
public class BrokerFactory implements InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(BrokerFactory.class);


    // ---------------------- instance ----------------------
    private static BrokerFactory instance;

    public static BrokerFactory getInstance() {
        return instance;
    }

    // ---------------------- resource ----------------------

    @Resource
    private BrokerService brokerService;
    @Autowired
    private MessageMapper messageMapper;
    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private AccessTokenMapper accessTokenMapper;
    @Resource
    private InstanceMapper instanceMapper;
    @Autowired
    private TopicMapper topicMapper;

    public MessageMapper getMessageMapper() {
        return messageMapper;
    }

    public ApplicationMapper getApplicationMapper() {
        return applicationMapper;
    }

    public AccessTokenMapper getAccessTokenMapper() {
        return accessTokenMapper;
    }

    public InstanceMapper getInstanceMapper() {
        return instanceMapper;
    }

    public TopicMapper getTopicMapper() {
        return topicMapper;
    }
    // ---------------------- start / stop ----------------------

    private AccessTokenThreadHelper accessTokenThreadHelper;
    private RegistryMessageQueueHelper registryMessageQueueHelper;
    private LocalCacheThreadHelper localCacheThreadHelper;
    private MessageHelper messageHelper;

    public AccessTokenThreadHelper getAccessTokenThreadHelper() {
        return accessTokenThreadHelper;
    }

    public RegistryMessageQueueHelper getRegistryMessageQueueHelper() {
        return registryMessageQueueHelper;
    }

    public LocalCacheThreadHelper getLocalCacheThreadHelper() {
        return localCacheThreadHelper;
    }

    public MessageHelper getMessageHelper() {
        return messageHelper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // base init
        instance = this;

        // 1、AccessTokenThread
        accessTokenThreadHelper = new AccessTokenThreadHelper(this);        // 1 thread
        accessTokenThreadHelper.start();

        // 2、Registry MessageQueue
        registryMessageQueueHelper = new RegistryMessageQueueHelper(this);  // 2 thread
        registryMessageQueueHelper.start();

        // 3、Registry LocalCache Thread
        localCacheThreadHelper = new LocalCacheThreadHelper(this);          // 1 thread
        localCacheThreadHelper.start();

        // 4、Produce MessageQueue
        messageHelper = new MessageHelper(this);                            // 50 + 20 + 1 thread
        messageHelper.start();

        // 6、ArchiveHelper ； TODO ，归档(清理实时表；) + 报表 + 邮件告警； 60s 一次；        // 1 thread
    }

    @Override
    public void destroy() throws Exception {
        // 1、AccessTokenThread
        accessTokenThreadHelper.stop();

        // 2、Registry MessageQueue
        registryMessageQueueHelper.stop();

        // 3、Registry LocalCache Thread
        localCacheThreadHelper.stop();

        // 4、Produce MessageQueue
        messageHelper.stop();
    }

    // ---------------------- openapi JsonRpcServer ----------------------

    @Bean
    public JsonRpcServer jsonRpcServer() {
        JsonRpcServer jsonRpcServer = new JsonRpcServer();
        jsonRpcServer.register("brokerService", brokerService);

        return jsonRpcServer;
    }

    // ---------------------- other ----------------------


}
