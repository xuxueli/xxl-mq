package com.xxl.mq.client.factory;

import com.xxl.mq.client.broker.IXxlMqBroker;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.consumer.thread.ConsumerThread;
import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class XxlMqClientFactory implements ApplicationContextAware, DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqClientFactory.class);


    // ---------------------- init destroy  ----------------------

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // init Broker
        initBrokerService();

        // init queue consumer
        initConsumer(applicationContext);
    }

    @Override
    public void destroy() throws Exception {

        // init queue consumer
        destoryConsumer();

        // destory Broker
        destoryBrokerService();
    }

    // ---------------------- broker service ----------------------

    private String zkaddress;
    private String zkdigest;
    private String env;

    public void setZkaddress(String zkaddress) {
        this.zkaddress = zkaddress;
    }
    public void setZkdigest(String zkdigest) {
        this.zkdigest = zkdigest;
    }
    public void setEnv(String env) {
        this.env = env;
    }

    private XxlRpcInvokerFactory xxlRpcInvokerFactory = null;

    private static ServiceRegistry serviceRegistry = null;
    private static IXxlMqBroker xxlMqBroker;
    public static ServiceRegistry getServiceRegistry() {
        return serviceRegistry;
    }
    public static IXxlMqBroker getXxlMqBroker() {
        return xxlMqBroker;
    }

    public void initBrokerService() {
        // init invoker factory
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
        serviceRegistry = XxlRpcInvokerFactory.getServiceRegistry();

        // init service reference
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
    private static void initConsumer(ApplicationContext applicationContext){

        // load consumer from spring
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(MqConsumer.class);
        if (serviceMap!=null && serviceMap.size()>0) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof IMqConsumer) {

                    // valid annotation
                    MqConsumer annotation = serviceBean.getClass().getAnnotation(MqConsumer.class);
                    if (annotation.group()==null || annotation.group().trim().length()==0) {
                        throw new RuntimeException("xxl-mq, MqConsumer("+ serviceBean.getClass() +"),group is empty.");
                    }
                    if (annotation.topic()==null || annotation.topic().trim().length()==0) {
                        throw new RuntimeException("xxl-mq, MqConsumer("+ serviceBean.getClass() +"),topic is empty.");
                    }

                    // consumer map
                    consumerRespository.add(new ConsumerThread((IMqConsumer) serviceBean));
                }
            }
        }

        if (consumerRespository ==null || consumerRespository.size()==0) {
            return;
        }

        // registry consumer, use xxl-job registry
        for (ConsumerThread item: consumerRespository) {
            ConsumerRegistryHelper.registerConsumer(item.getMqConsumer());
        }

        // consumer thread start

        for (ConsumerThread item: consumerRespository) {
            consumerExecutorService.submit(item);
            logger.info(">>>>>>>>>>> xxl-mq, consumer init success, , topic:{}, group:{}", item.getMqConsumer().topic(), item.getMqConsumer().group());
        }
    }

    /**
     * destory consumer
     */
    private void destoryConsumer(){
        if (consumerRespository ==null || consumerRespository.size()==0) {
            return;
        }

        consumerExecutorStoped = true;
        consumerExecutorService.shutdownNow();
    }

}
