package com.xxl.mq.client.factory;

import com.xxl.mq.client.broker.IXxlMqBroker;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.xxl.mq.client.consumer.thread.ConsumerThread;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.rpc.registry.ServiceRegistry;
import com.xxl.rpc.registry.impl.ZkServiceRegistry;
import com.xxl.rpc.remoting.invoker.XxlRpcInvokerFactory;
import com.xxl.rpc.remoting.invoker.call.CallType;
import com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean;
import com.xxl.rpc.remoting.net.NetEnum;
import com.xxl.rpc.serialize.Serializer;
import com.xxl.rpc.util.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author xuxueli 2018-11-18 21:18:10
 */
public class XxlMqClientFactory  {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqClientFactory.class);


    // ---------------------- param  ----------------------

    private String zkaddress;
    private String zkdigest;
    private String env;
    private List<IMqConsumer> consumerList;

    public void setZkaddress(String zkaddress) {
        this.zkaddress = zkaddress;
    }
    public void setZkdigest(String zkdigest) {
        this.zkdigest = zkdigest;
    }
    public void setEnv(String env) {
        this.env = env;
    }
    public void setConsumerList(List<IMqConsumer> consumerList) {
        this.consumerList = consumerList;
    }

    // ---------------------- init destroy  ----------------------

    public void init() {
        // valid ConsumerThread
        validConsumerThread();

        // start BrokerService
        startBrokerService();

        // submit ConsumerThread
        submitConsumerThread();
    }

    public void destroy() throws Exception {

        // destory ClientFactoryThreadPool
        destoryClientFactoryThreadPool();

        // destory BrokerService
        destoryBrokerService();
    }


    // ---------------------- thread pool ----------------------

    private ExecutorService clientFactoryThreadPool = Executors.newCachedThreadPool();
    public static volatile boolean clientFactoryPoolStoped = false;

    /**
     * destory consumer thread
     */
    private void destoryClientFactoryThreadPool(){
        clientFactoryPoolStoped = true;
        clientFactoryThreadPool.shutdownNow();
    }


    // ---------------------- broker service ----------------------

    private XxlRpcInvokerFactory xxlRpcInvokerFactory = null;

    private static IXxlMqBroker xxlMqBroker;
    private static ConsumerRegistryHelper consumerRegistryHelper = null;
    private static LinkedBlockingQueue<XxlMqMessage> newMessageQueue = new LinkedBlockingQueue<>();

    public static IXxlMqBroker getXxlMqBroker() {
        return xxlMqBroker;
    }
    public static ConsumerRegistryHelper getConsumerRegistryHelper() {
        return consumerRegistryHelper;
    }
    public static void addMessages(XxlMqMessage mqMessage, boolean async){
        if (async) {
            // async queue, mult send
            newMessageQueue.add(mqMessage);
        } else {
            // sync rpc, one send
            xxlMqBroker.addMessages(Arrays.asList(mqMessage));
        }

    }

    public void startBrokerService() {
        // init XxlRpcInvokerFactory
        xxlRpcInvokerFactory = new XxlRpcInvokerFactory(ZkServiceRegistry.class, new HashMap<String, String>(){{
            put(Environment.ZK_ADDRESS, zkaddress);
            put(Environment.ZK_DIGEST, zkdigest);
            put(Environment.ENV, "xxl-mq#"+env);
        }});
        try {
            xxlRpcInvokerFactory.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // init ConsumerRegistryHelper
        ServiceRegistry serviceRegistry = XxlRpcInvokerFactory.getServiceRegistry();
        consumerRegistryHelper = new ConsumerRegistryHelper(serviceRegistry);

        // init IXxlMqBroker
        xxlMqBroker = (IXxlMqBroker) new XxlRpcReferenceBean(NetEnum.NETTY, Serializer.SerializeEnum.HESSIAN.getSerializer(), CallType.SYNC,
                IXxlMqBroker.class, null, 10000, null, null, null).getObject();

        //
        for (int i = 0; i < 10; i++) {
            clientFactoryThreadPool.execute(new Runnable() {
                @Override
                public void run() {

                    while (!XxlMqClientFactory.clientFactoryPoolStoped) {
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
                                xxlMqBroker.addMessages(messageList);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
        }

    }
    public void destoryBrokerService() throws Exception {
        // stop invoker factory
        if (xxlRpcInvokerFactory != null) {
            xxlRpcInvokerFactory.stop();
        }
    }



    // ---------------------- queue consumer ----------------------

    // queue consumer respository
    private List<ConsumerThread> consumerRespository = new ArrayList<ConsumerThread>();

    /**
     * valid ConsumerThread
     */
    private void validConsumerThread(){

        // valid
        if (consumerList==null || consumerList.size()==0) {
            throw new RuntimeException("xxl-mq, MqConsumer not found.");
        }

        // valid data
        for (IMqConsumer consumer : consumerList) {
            // valid annotation
            MqConsumer annotation = consumer.getClass().getAnnotation(MqConsumer.class);
            if (annotation == null) {
                throw new RuntimeException("xxl-mq, MqConsumer("+ consumer.getClass() +"),annotation is not exists.");
            }
            if (annotation.group()==null || annotation.group().trim().length()==0) {
                throw new RuntimeException("xxl-mq, MqConsumer("+ consumer.getClass() +"),group is empty.");
            }
            if (annotation.topic()==null || annotation.topic().trim().length()==0) {
                throw new RuntimeException("xxl-mq, MqConsumer("+ consumer.getClass() +"),topic is empty.");
            }

            // consumer map
            consumerRespository.add(new ConsumerThread(consumer));
        }
    }

    /**
     * submit ConsumerThread
     */
    private void submitConsumerThread(){

        // valid
        if (consumerRespository ==null || consumerRespository.size()==0) {
            return;
        }

        // registry
        for (ConsumerThread item: consumerRespository) {
            getConsumerRegistryHelper().registerConsumer(item);
        }

        // consumer
        for (ConsumerThread item: consumerRespository) {
            clientFactoryThreadPool.execute(item);
            logger.info(">>>>>>>>>>> xxl-mq, consumer init success, , topic:{}, group:{}", item.getMqConsumer().topic(), item.getMqConsumer().group());
        }
    }



}
