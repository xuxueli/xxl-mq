package com.xxl.mq.admin.broker.config;

import com.xxl.mq.admin.broker.thread.*;
import com.xxl.mq.admin.mapper.*;
import com.xxl.mq.admin.service.MessageService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;


/**
 * registry config
 *
 * @author xuxueli
 */
@Configuration
public class BrokerBootstrap implements InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(BrokerBootstrap.class);


    // ---------------------- instance ----------------------
    private static BrokerBootstrap instance;

    public static BrokerBootstrap getInstance() {
        return instance;
    }

    // ---------------------- resource ----------------------

    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${server.port}")
    private int port;

    /*@Resource
    private BrokerService brokerService;*/
    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private MessageArchiveMapper messageArchiveMapper;
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

    public String getMailUsername() {
        return mailUsername;
    }

    public int getPort() {
        return port;
    }

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

    public MessageArchiveMapper getMessageArchiveMapper() {
        return messageArchiveMapper;
    }

    // ---------------------- start / stop ----------------------

    private AccessTokenThreadHelper accessTokenThreadHelper;
    private RegistryMessageQueueHelper registryMessageQueueHelper;
    private LocalCacheThreadHelper localCacheThreadHelper;
    private MessageProduceAndConsumeThreadHelper messageProduceAndConsumeThreadHelper;
    private ArchiveAndAlarmThreadHelper archiveThreadHelper;
    private FailMessageProcessThreadHelper failMessageProcessThreadHelper;

    public AccessTokenThreadHelper getAccessTokenThreadHelper() {
        return accessTokenThreadHelper;
    }

    public RegistryMessageQueueHelper getRegistryMessageQueueHelper() {
        return registryMessageQueueHelper;
    }

    public LocalCacheThreadHelper getLocalCacheThreadHelper() {
        return localCacheThreadHelper;
    }

    public MessageProduceAndConsumeThreadHelper getMessageProduceAndConsumeThreadHelper() {
        return messageProduceAndConsumeThreadHelper;
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
        messageProduceAndConsumeThreadHelper = new MessageProduceAndConsumeThreadHelper(this);                // 50 + 20/ poll； 1 thread / 60s
        messageProduceAndConsumeThreadHelper.start();

        // 5、ArchiveHelper ；                                                           // 1 thread   / 60s
        archiveThreadHelper = new ArchiveAndAlarmThreadHelper(this);
        archiveThreadHelper.start();

        // 6、FailMessageProcessThreadHelper
        failMessageProcessThreadHelper = new FailMessageProcessThreadHelper(this);
        failMessageProcessThreadHelper.start();
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
        messageProduceAndConsumeThreadHelper.stop();

        // 5、ArchiveHelper
        archiveThreadHelper.stop();

        // 6、FailMessageProcessThreadHelper
        failMessageProcessThreadHelper.stop();
    }

    // ---------------------- openapi JsonRpcServer ----------------------

    /*@Bean
    public JsonRpcServer jsonRpcServer() {
        JsonRpcServer jsonRpcServer = JsonRpcServer.newServer();
        jsonRpcServer.register("brokerService", brokerService);

        return jsonRpcServer;
    }*/

    // ---------------------- other ----------------------


}
