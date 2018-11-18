package com.xxl.mq.client.factory;

import com.xxl.mq.client.broker.IXxlMqBroker;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.xxl.mq.client.consumer.thread.ConsumerThread;
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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        // init ConsumerThread
        initConsumerThread();

        // start BrokerService
        startBrokerService();

        // start ConsumerThread
        startConsumerThread();
    }

    public void destroy() throws Exception {

        // destory ConsumerThread
        destoryConsumerThread();

        // destory BrokerService
        destoryBrokerService();
    }


    // ---------------------- broker service ----------------------

    private XxlRpcInvokerFactory xxlRpcInvokerFactory = null;

    private static IXxlMqBroker xxlMqBroker;
    private static ConsumerRegistryHelper consumerRegistryHelper = null;
    public static IXxlMqBroker getXxlMqBroker() {
        return xxlMqBroker;
    }
    public static ConsumerRegistryHelper getConsumerRegistryHelper() {
        return consumerRegistryHelper;
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

    }
    public void destoryBrokerService() throws Exception {
        // stop invoker factory
        if (xxlRpcInvokerFactory != null) {
            xxlRpcInvokerFactory.stop();
        }
    }


    // ---------------------- queue consumer ----------------------

    public static volatile boolean consumerExecutorStoped = false;
    private static ExecutorService consumerExecutorService = Executors.newCachedThreadPool();

    // queue consumer respository
    private static List<ConsumerThread> consumerRespository = new ArrayList<ConsumerThread>();

    /**
     * init consumer
     */
    private void initConsumerThread(){
        if (consumerList==null || consumerList.size()==0) {
            throw new RuntimeException("xxl-mq, MqConsumer not found.");
        }
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
     * start consumer thread
     */
    private void startConsumerThread(){
        if (consumerRespository ==null || consumerRespository.size()==0) {
            return;
        }

        // registry consumer, use xxl-job registry
        for (ConsumerThread item: consumerRespository) {
            getConsumerRegistryHelper().registerConsumer(item.getMqConsumer());
        }

        // consumer thread start

        for (ConsumerThread item: consumerRespository) {
            consumerExecutorService.submit(item);
            logger.info(">>>>>>>>>>> xxl-mq, consumer init success, , topic:{}, group:{}", item.getMqConsumer().topic(), item.getMqConsumer().group());
        }
    }

    /**
     * destory consumer thread
     */
    private void destoryConsumerThread(){
        if (consumerRespository ==null || consumerRespository.size()==0) {
            return;
        }

        consumerExecutorStoped = true;
        consumerExecutorService.shutdownNow();
    }

}
