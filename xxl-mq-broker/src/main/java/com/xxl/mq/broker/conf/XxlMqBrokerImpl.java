package com.xxl.mq.broker.conf;

import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.client.broker.IXxlMqBroker;
import com.xxl.mq.client.message.MessageStatus;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.util.DateFormatUtil;
import com.xxl.rpc.registry.impl.ZkServiceRegistry;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.remoting.provider.XxlRpcProviderFactory;
import com.xxl.rpc.serialize.Serializer;
import com.xxl.rpc.util.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.MessageFormat;
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

    @Value("${xxl-rpc.remoting.port:0}")
    private int port;

    @Value("${xxl-rpc.registry.zk.zkaddress:}")
    private String zkaddress;

    @Value("${xxl-rpc.registry.zk.zkdigest:}")
    private String zkdigest;

    @Value("${xxl-rpc.env:}")
    private String env;


    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;


    // ---------------------- broker server ----------------------
    private XxlRpcProviderFactory providerFactory;

    @Override
    public void afterPropertiesSet() throws Exception {

        // server init
        providerFactory = new XxlRpcProviderFactory();
        providerFactory.initConfig(NetEnum.NETTY, Serializer.SerializeEnum.HESSIAN.getSerializer(), null, port, null, ZkServiceRegistry.class, new HashMap<String, String>(){{
            put(Environment.ZK_ADDRESS, zkaddress);
            put(Environment.ZK_DIGEST, zkdigest);
            put(Environment.ENV, env);
        }});

        // server add
        providerFactory.addService(IXxlMqBroker.class.getName(), "xxl-mq", this);

        // server start
        providerFactory.start();

        // broker thread
        initThead();
    }

    @Override
    public void destroy() throws Exception {

        // server stop
        providerFactory.stop();


        // broker thread
        destroyThread();
    }

    // ---------------------- broker thread ----------------------

    private LinkedBlockingQueue<XxlMqMessage> newMessageQueue = new LinkedBlockingQueue<XxlMqMessage>();
    private LinkedBlockingQueue<XxlMqMessage> consumeCallbackMessageQueue = new LinkedBlockingQueue<XxlMqMessage>();

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean executorStoped = false;

    public void initThead() throws Exception {

        // init base broker biz
        /**
         * async save message
         */
        // TODO, mult thread
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        XxlMqMessage msg = newMessageQueue.take();
                        xxlMqMessageDao.save(msg);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }

                // TODO, mult clean msg

            }
        });
        /**
         * async consume message callback process
         */
        // TODO, mult thread
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while (!executorStoped) {
                    try {
                        XxlMqMessage message = consumeCallbackMessageQueue.take();
                        xxlMqMessageDao.updateStatus(message.getId(), message.getStatus(), message.getMsg());
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }

                // TODO, mult clean msg

            }
        });

        /**
         * 重试消息处理
         */
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                int waitTim = 5;
                while (!executorStoped) {
                    try {
                        List<Integer> msgIds = xxlMqMessageDao.retryMessageIds(100, MessageStatus.FAIL.name());
                        if (msgIds!=null && msgIds.size()>0) {
                            waitTim = 5;
                            for (int id: msgIds) {
                                String addMsg = MessageFormat.format("<hr>》》》时间: {0} <br>》》》操作: 失败消息触发重试,状态自动还原,剩余重试次数-1(status>>>NEW)", DateFormatUtil.formatDateTime(new Date()));
                                xxlMqMessageDao.retryStatusFresh(id, addMsg, MessageStatus.FAIL.name(), MessageStatus.NEW.name());
                            }
                        } else {
                            waitTim += 5;
                            if (waitTim>60) {
                                waitTim = 60;
                            }
                        }

                        TimeUnit.SECONDS.sleep(waitTim);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        });

    }
    public void destroyThread(){
        executorService.shutdown();
    }


    // ---------------------- broker api ----------------------

    @Override
    public int saveMessage(XxlMqMessage message) {
        return newMessageQueue.add(message)?1:-1;
    }

    @Override
    public List<XxlMqMessage> pullNewMessage(String name, int pagesize, int consumerRank, int consumerTotal) {
        List<XxlMqMessage> list = xxlMqMessageDao.pullNewMessage(name, MessageStatus.NEW.name(), pagesize, consumerRank, consumerTotal);
        return list;
    }

    @Override
    public int lockMessage(int id, String addMsg) {
        return xxlMqMessageDao.lockMessage(id, addMsg, MessageStatus.NEW.name(), MessageStatus.ING.name());
    }

    public int consumeCallbackMessage(XxlMqMessage message) {
        return consumeCallbackMessageQueue.add(message)?1:-1;
    }

}
