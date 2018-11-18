package com.xxl.mq.broker.conf;

import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.client.broker.IXxlMqBroker;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.util.DateFormatUtil;
import com.xxl.rpc.registry.impl.ZkServiceRegistry;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.remoting.provider.XxlRpcProviderFactory;
import com.xxl.rpc.serialize.Serializer;
import com.xxl.rpc.util.Environment;
import com.xxl.rpc.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/8/28.
 */
@Component
public class XxlMqBrokerImpl implements IXxlMqBroker, InitializingBean, DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqBrokerImpl.class);


    // ---------------------- param ----------------------

    @Value("${xxl-mq.rpc.remoting.port}")
    private int port;

    @Value("${xxl-mq.rpc.registry.zk.zkaddress}")
    private String zkaddress;

    @Value("${xxl-mq.rpc.registry.zk.zkdigest}")
    private String zkdigest;

    @Value("${xxl-mq.rpc.registry.zk.env}")
    private String env;

    @Value("${xxl-mq.log.logretentiondays}")
    private int logretentiondays;

    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;


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

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean executorStoped = false;

    public void initThead() throws Exception {

        /**
         * async save message, mult thread
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
                            logger.error(e.getMessage(), e);
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
         * async callback message, mult thread
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
                            }

                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
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
         * async retry message
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        // mult retry message
                        String appendLog = MessageFormat.format("<hr>》》》时间: {0} <br>》》》操作: 失败消息触发重试,状态自动还原,剩余重试次数减一", DateFormatUtil.formatDateTime(new Date()));
                        int count = xxlMqMessageDao.updateRetryCount(XxlMqMessageStatus.FAIL.name(), XxlMqMessageStatus.NEW.name(), appendLog);
                        if (count > 0) {
                            logger.info("xxl-mq, retry message, count:{}", count);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        // sleep
                        TimeUnit.SECONDS.sleep(30);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });

        /**
         * async clean success message
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
                            logger.error(e.getMessage(), e);
                        }
                        try {
                            TimeUnit.DAYS.sleep(logretentiondays);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
        }

    }
    public void destroyThread(){
        executorService.shutdownNow();
    }


    // ---------------------- broker server ----------------------

    private XxlRpcProviderFactory providerFactory;

    public void initServer() throws Exception {
        // init server
        providerFactory = new XxlRpcProviderFactory();
        providerFactory.initConfig(NetEnum.NETTY, Serializer.SerializeEnum.HESSIAN.getSerializer(), IpUtil.getIp(), port, null, ZkServiceRegistry.class, new HashMap<String, String>(){{
            put(Environment.ZK_ADDRESS, zkaddress);
            put(Environment.ZK_DIGEST, zkdigest);
            put(Environment.ENV, "xxl-mq#"+env);
        }});

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
        return xxlMqMessageDao.lockMessage(id, appendLog, XxlMqMessageStatus.NEW.name(), XxlMqMessageStatus.ING.name());
    }

    @Override
    public int callbackMessages(List<XxlMqMessage> messages) {
        callbackMessageQueue.addAll(messages);
        return messages.size();
    }

}
