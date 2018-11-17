package com.xxl.mq.client.factory;

import com.xxl.mq.client.broker.IXxlMqBroker;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.consumer.thread.ConsumerThread;
import com.xxl.mq.client.topic.TopicHelper;
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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class XxlMqClientFactory implements ApplicationContextAware, DisposableBean {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqClientFactory.class);


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
    private ServiceRegistry serviceRegistry = null;
    private static IXxlMqBroker xxlMqBroker;
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        // init Broker
        initBrokerService();

        // init consumer resp
        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(MqConsumer.class);
        if (serviceMap!=null && serviceMap.size()>0) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof IMqConsumer) {
                    // valid annotation
                    MqConsumer annotation = serviceBean.getClass().getAnnotation(MqConsumer.class);
                    String name = annotation.value();
                    if (annotation.group()==null || annotation.group().trim().length()==0 || name==null || name.trim().length()==0) {
                        continue;
                    }
                    // consumer map
                    consumerRespository.put(name, new ConsumerThread((IMqConsumer) serviceBean));
                }
            }
        }

        // init queue consumer
        initQueueConsumer();

    }

    @Override
    public void destroy() throws Exception {

        // destory Broker
        destoryBrokerService();
    }

    // ---------------------- queue consumer ----------------------

    // fresh consumer
    private static Executor executor = Executors.newCachedThreadPool();

    // queue consumer respository
    private static ConcurrentHashMap<String, ConsumerThread> consumerRespository = new ConcurrentHashMap<String, ConsumerThread>();


    /**
     * init queue consumer
     */
    private static void initQueueConsumer(){
        if (consumerRespository ==null || consumerRespository.size()==0) {
            return;
        }

        // registry consumer, and fresh each 60s
        try {
            TopicHelper.registerConsumers(consumerRespository.keySet());
        } catch (Exception e) {
            logger.error("", e);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // registry
                    try {
                        TimeUnit.SECONDS.sleep(60);
                        TopicHelper.registerConsumers(consumerRespository.keySet());
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        });

        // consumer thread start
        for (Map.Entry<String, ConsumerThread> item: consumerRespository.entrySet()) {
            item.getValue().start();
            MqConsumer annotation = item.getValue().getConsumerHandler().getClass().getAnnotation(MqConsumer.class);
            logger.info(">>>>>>>>>>> xxl-mq, queue consumer thread start, annotation={}", annotation);
        }
    }


}
