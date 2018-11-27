package com.xxl.mq.admin.conf;

import com.xxl.mq.admin.core.model.XxlCommonRegistryData;
import com.xxl.mq.admin.core.model.XxlMqTopic;
import com.xxl.mq.admin.dao.IXxlMqMessageDao;
import com.xxl.mq.admin.dao.IXxlMqTopicDao;
import com.xxl.mq.admin.service.IXxlMqTopicService;
import com.xxl.mq.admin.service.impl.XxlCommonRegistryServiceImpl;
import com.xxl.mq.client.broker.IXxlMqBroker;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.util.LogHelper;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.remoting.provider.XxlRpcProviderFactory;
import com.xxl.rpc.serialize.Serializer;
import com.xxl.rpc.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by xuxueli on 16/8/28.
 */
@Component
public class XxlMqBrokerImpl implements IXxlMqBroker, InitializingBean, DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqBrokerImpl.class);


    // ---------------------- param ----------------------

    @Value("${xxl-mq.rpc.remoting.port}")
    private int port;

    @Value("${xxl.mq.log.logretentiondays}")
    private int logretentiondays;


    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;
    @Resource
    private IXxlMqTopicService xxlMqTopicService;
    @Resource
    private IXxlMqTopicDao xxlMqTopicDao;

    @Resource
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String emailUserName;


    // ---------------------- broker server ----------------------

    @Override
    public void afterPropertiesSet() throws Exception {
        // init server
        initServer();

        // init thread
        initThead();
    }

    @Override
    public void destroy() throws Exception {

        // destory server
        destoryServer();

        // destory thread
        destroyThread();
    }

    // ---------------------- broker thread ----------------------

    private LinkedBlockingQueue<XxlMqMessage> newMessageQueue = new LinkedBlockingQueue<XxlMqMessage>();
    private LinkedBlockingQueue<XxlMqMessage> callbackMessageQueue = new LinkedBlockingQueue<XxlMqMessage>();
    private Map<String, Long> alarmMessageInfo = new ConcurrentHashMap<String, Long>();

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean executorStoped = false;

    public void initThead() throws Exception {

        /**
         * async save message, mult thread  (by event)
         */
        for (int i = 0; i < 3; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!executorStoped) {
                        try {
                            XxlMqMessage message = newMessageQueue.take();
                            if (message != null) {
                                // load
                                List<XxlMqMessage> messageList = new ArrayList<>();
                                messageList.add(message);

                                List<XxlMqMessage> otherMessageList = new ArrayList<>();
                                int drainToNum = newMessageQueue.drainTo(otherMessageList, 100);
                                if (drainToNum > 0) {
                                    messageList.addAll(otherMessageList);
                                }

                                // save
                                xxlMqMessageDao.save(messageList);
                            }
                        } catch (Exception e) {
                            if (!executorStoped) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }

                    // end save
                    List<XxlMqMessage> otherMessageList = new ArrayList<>();
                    int drainToNum = newMessageQueue.drainTo(otherMessageList);
                    if (drainToNum> 0) {
                        xxlMqMessageDao.save(otherMessageList);
                    }

                }
            });
        }

        /**
         * async callback message, mult thread  (by event)
         */
        for (int i = 0; i < 3; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!executorStoped) {
                        try {
                            XxlMqMessage message = callbackMessageQueue.take();
                            if (message!=null) {
                                // load
                                List<XxlMqMessage> messageList = new ArrayList<>();
                                messageList.add(message);

                                List<XxlMqMessage> otherMessageList = new ArrayList<>();
                                int drainToNum = callbackMessageQueue.drainTo(otherMessageList, 100);
                                if (drainToNum > 0) {
                                    messageList.addAll(otherMessageList);
                                }

                                // save
                                xxlMqMessageDao.updateStatus(messageList);

                                // fill alarm info
                                for (XxlMqMessage alarmItem: messageList) {
                                    if (XxlMqMessageStatus.FAIL.name().equals(alarmItem.getStatus())) {
                                        Long failCount = alarmMessageInfo.get(alarmItem.getTopic());
                                        failCount = failCount!=null?failCount++:1;
                                        alarmMessageInfo.put(alarmItem.getTopic(), failCount);
                                    }
                                }

                            }

                        } catch (Exception e) {
                            if (!executorStoped) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }

                    // end save
                    List<XxlMqMessage> otherMessageList = new ArrayList<>();
                    int drainToNum = callbackMessageQueue.drainTo(otherMessageList);
                    if (drainToNum > 0) {
                        xxlMqMessageDao.updateStatus(otherMessageList);
                    }

                }
            });
        }


        /**
         * auto retry message "retryCount-1 + status change"  (by cycle, 1/60s)
         *
         * auto reset block timeout message "check block + status change"  (by cycle, 1/60s)
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        // mult retry message
                        String appendLog = LogHelper.makeLog("失败重试", "状态自动还原,剩余重试次数减一");
                        int count = xxlMqMessageDao.updateRetryCount(XxlMqMessageStatus.FAIL.name(), XxlMqMessageStatus.NEW.name(), appendLog);
                        if (count > 0) {
                            logger.info("xxl-mq, retry message, count:{}", count);
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        // mult reset block message
                        String appendLog = LogHelper.makeLog("阻塞清理", "状态自动标记失败");
                        int count = xxlMqMessageDao.resetBlockTimeoutMessage(XxlMqMessageStatus.RUNNING.name(), XxlMqMessageStatus.FAIL.name(), appendLog);
                        if (count > 0) {
                            logger.info("xxl-mq, retry block message, count:{}", count);
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        // sleep
                        TimeUnit.SECONDS.sleep(60);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

        /**
         * auto alarm "check topic fail count, send alarm"  (by cycle, 1/60s)
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        // mult send alarm
                        if (alarmMessageInfo.size() > 0) {

                            // copy
                            Map<String, Long> alarmMessageInfoTmp = new HashMap<String, Long>();
                            alarmMessageInfoTmp.putAll(alarmMessageInfo);
                            alarmMessageInfo.clear();

                            // alarm
                            List<XxlMqTopic> topicList = xxlMqTopicDao.findAlarmByTopic(new ArrayList<String>(alarmMessageInfoTmp.keySet()));
                            if (topicList!=null && topicList.size()>0) {
                                for (XxlMqTopic mqTopic: topicList) {
                                    if (mqTopic.getAlarmEmails()!=null && mqTopic.getAlarmEmails().trim().length()>0) {
                                        Long failCount = alarmMessageInfoTmp.get(mqTopic.getTopic());

                                        String[] toEmailList = null;
                                        if (mqTopic.getAlarmEmails().contains(",")) {
                                            toEmailList = mqTopic.getAlarmEmails().split(",");
                                        } else {
                                            toEmailList = new String[]{mqTopic.getAlarmEmails()};
                                        }
                                        String emailContent = MessageFormat.format("告警类型：消息失败；<br>Topic：{0}；<br>备注：{1}",
                                                mqTopic.getTopic(), "1min内失败消息数量=" + failCount);

                                        // make mail
                                        try {
                                            MimeMessage mimeMessage = mailSender.createMimeMessage();

                                            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                                            helper.setFrom(emailUserName, "分布式消息队列XXL-MQ");
                                            helper.setTo(toEmailList);
                                            helper.setSubject("消息队列中心监控报警");
                                            helper.setText(emailContent, true);

                                            mailSender.send(mimeMessage);
                                        } catch (Exception e) {
                                            logger.error(">>>>>>>>>>> message monitor alarm email send error, topic:{}, failCount:{}", mqTopic.getTopic(), failCount);
                                        }

                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        // sleep
                        TimeUnit.MINUTES.sleep(1);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

        /**
         * auto clean success message  (by cycle, 1/>=3day)
         */
        if (logretentiondays >= 3) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    while (!executorStoped) {
                        try {
                            int count = xxlMqMessageDao.cleanSuccessMessage(XxlMqMessageStatus.SUCCESS.name(), logretentiondays);
                            logger.info("xxl-mq, clean success message, count:{}", count);
                        } catch (Exception e) {
                            if (!executorStoped) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                        try {
                            TimeUnit.DAYS.sleep(logretentiondays);
                        } catch (Exception e) {
                            if (!executorStoped) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }
            });
        }

        /**
         * auto find new topic from message  (by cycle, 1+N/1min)
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        // find new topic, set messageInfo
                        List<String> topicList = xxlMqMessageDao.findNewTopicList();
                        if (topicList!=null && topicList.size()>0) {
                            for (String topic:topicList) {
                                XxlMqTopic newTopic = new XxlMqTopic();
                                newTopic.setTopic(topic);
                                xxlMqTopicService.add(newTopic);
                            }
                        }

                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.MINUTES.sleep(1);
                    } catch (Exception e) {
                        if (!executorStoped) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        });

    }
    public void destroyThread(){
        executorStoped = true;
        executorService.shutdownNow();
    }


    // ---------------------- broker server ----------------------

    private XxlRpcProviderFactory providerFactory;

    public void initServer() throws Exception {

        // address, static registry
        String ip = IpUtil.getIp();
        String address = IpUtil.getIpPort(ip, port);
        XxlCommonRegistryData xxlCommonRegistryData = new XxlCommonRegistryData();
        xxlCommonRegistryData.setKey(IXxlMqBroker.class.getName());
        xxlCommonRegistryData.setValue(address);
        XxlCommonRegistryServiceImpl.staticRegistryData = xxlCommonRegistryData;


        // init server
        providerFactory = new XxlRpcProviderFactory();
        providerFactory.initConfig(NetEnum.NETTY, Serializer.SerializeEnum.HESSIAN.getSerializer(), ip, port, null, null, null);

        // add server
        providerFactory.addService(IXxlMqBroker.class.getName(), null, this);

        // start server
        providerFactory.start();
    }

    public void destoryServer() throws Exception {
        // stop server
        if (providerFactory != null) {
            providerFactory.stop();
        }
    }


    // ---------------------- broker api ----------------------

    @Override
    public int addMessages(List<XxlMqMessage> messages) {
        newMessageQueue.addAll(messages);
        return messages.size();
    }

    @Override
    public List<XxlMqMessage> pullNewMessage(String topic, String group, int consumerRank, int consumerTotal, int pagesize) {
        List<XxlMqMessage> list = xxlMqMessageDao.pullNewMessage(XxlMqMessageStatus.NEW.name(), topic, group, consumerRank, consumerTotal, pagesize);
        return list;
    }

    @Override
    public int lockMessage(long id, String appendLog) {
        return xxlMqMessageDao.lockMessage(id, appendLog, XxlMqMessageStatus.NEW.name(), XxlMqMessageStatus.RUNNING.name());
    }

    @Override
    public int callbackMessages(List<XxlMqMessage> messages) {
        callbackMessageQueue.addAll(messages);
        return messages.size();
    }

}
