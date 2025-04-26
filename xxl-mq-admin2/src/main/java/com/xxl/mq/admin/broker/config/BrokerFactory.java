package com.xxl.mq.admin.broker.config;

import com.xxl.mq.admin.broker.thread.*;
import com.xxl.mq.admin.mapper.*;
import com.xxl.mq.admin.service.MessageService;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.tool.jsonrpc.JsonRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

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
    @Autowired
    private MessageService messageService;
    @Resource
    private JavaMailSender mailSender;
    @Resource
    private MessageReportMapper messageReportMapper;

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
    public MessageService getMessageService() {
        return messageService;
    }

    public JavaMailSender getMailSender() {
        return mailSender;
    }

    public MessageReportMapper getMessageReportMapper() {
        return messageReportMapper;
    }

    // ---------------------- start / stop ----------------------

    private AccessTokenThreadHelper accessTokenThreadHelper;
    private RegistryMessageQueueHelper registryMessageQueueHelper;
    private LocalCacheThreadHelper localCacheThreadHelper;
    private MessageThreadHelper messageThreadHelper;
    private ArchiveThreadHelper archiveThreadHelper;

    public AccessTokenThreadHelper getAccessTokenThreadHelper() {
        return accessTokenThreadHelper;
    }

    public RegistryMessageQueueHelper getRegistryMessageQueueHelper() {
        return registryMessageQueueHelper;
    }

    public LocalCacheThreadHelper getLocalCacheThreadHelper() {
        return localCacheThreadHelper;
    }

    public MessageThreadHelper getMessageThreadHelper() {
        return messageThreadHelper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // base init
        instance = this;

        // 1、AccessTokenThread
        accessTokenThreadHelper = new AccessTokenThreadHelper(this);        // 1 thread / 30s
        accessTokenThreadHelper.start();

        // 2、Registry MessageQueue
        registryMessageQueueHelper = new RegistryMessageQueueHelper(this);  // 2 thread / poll
        registryMessageQueueHelper.start();

        // 3、Registry LocalCache Thread
        localCacheThreadHelper = new LocalCacheThreadHelper(this);          // 1 thread / 30s
        localCacheThreadHelper.start();

        // 4、Produce MessageQueue
        messageThreadHelper = new MessageThreadHelper(this);                // 50 + 20/ poll； 1 thread / 60s
        messageThreadHelper.start();

        // 5、ArchiveHelper ；                                                           // 1 thread   / 60s
        archiveThreadHelper = new ArchiveThreadHelper(this);
        archiveThreadHelper.start();
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
        messageThreadHelper.stop();

        // 5、ArchiveHelper
        archiveThreadHelper.stop();
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
