package com.xxl.mq.client;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumerType;
import com.xxl.mq.client.consumer.thread.QueueConsumerThread;
import com.xxl.mq.client.consumer.thread.TopicConsumerThread;
import com.xxl.mq.client.rpc.util.ZkConsumerUtil;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqConsumer implements ApplicationContextAware {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqConsumer.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, IMqConsumer> topicConsumerMap = new HashMap<String, IMqConsumer>();

        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(MqConsumer.class);
        if (serviceMap!=null && serviceMap.size()>0) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof IMqConsumer) {
                    // valid annotation
                    MqConsumer annotation = serviceBean.getClass().getAnnotation(MqConsumer.class);
                    MqConsumerType type = annotation.type();
                    String name = annotation.value();
                    if (type==null || name==null || name.trim().length()==0) {
                        continue;
                    }
                    // consumer map
                    if (type==MqConsumerType.QUEUE || type==MqConsumerType.SERIAL_QUEUE) {
                        queueConsumerRespository.put(name, new QueueConsumerThread((IMqConsumer) serviceBean));
                    } else if (type==MqConsumerType.TOPIC){
                        topicConsumerRespository.put(name, new TopicConsumerThread((IMqConsumer) serviceBean));
                    }
                }
            }
        }

        // init queue consumer
        initQueueConsumer();

        // init topic consumer
        initTopicConsumer();
    }

    // fresh consumer
    private static Executor executor = Executors.newCachedThreadPool();

    // queue consumer respository
    private static ConcurrentHashMap<String, QueueConsumerThread> queueConsumerRespository = new ConcurrentHashMap<String, QueueConsumerThread>();


    /**
     * init queue consumer
     */
    private static void initQueueConsumer(){
        if (queueConsumerRespository==null || queueConsumerRespository.size()==0) {
            return;
        }

        // registry consumer, and fresh each 60s
        try {
            ZkConsumerUtil.registerConsumers(queueConsumerRespository.keySet());
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
                        ZkConsumerUtil.registerConsumers(queueConsumerRespository.keySet());
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        });

        // consumer thread start
        for (Map.Entry<String, QueueConsumerThread> item: queueConsumerRespository.entrySet()) {
            item.getValue().start();
            MqConsumer annotation = item.getValue().getConsumerHandler().getClass().getAnnotation(MqConsumer.class);
            logger.info(">>>>>>>>>>> xxl-mq, queue consumer thread start, annotation={}", annotation);
        }
    }


    // topic consumer respository
    private static ConcurrentHashMap<String, TopicConsumerThread> topicConsumerRespository = new ConcurrentHashMap<String, TopicConsumerThread>();

    /**
     * init topic consumer
     */
    private static void initTopicConsumer(){
        if (topicConsumerRespository==null || topicConsumerRespository.size()==0) {
            return;
        }
    }

}
