package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.util.ZkServiceUtil;
import com.xxl.mq.client.service.ConsumerHandler;
import com.xxl.mq.client.service.annotation.MqConsumer;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.LinkedList;
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

        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(MqConsumer.class);
        if (serviceMap!=null && serviceMap.size()>0) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof ConsumerHandler) {
                    // valid
                    MqConsumer annotation = serviceBean.getClass().getAnnotation(MqConsumer.class);
                    MqConsumer.MqType type = annotation.type();
                    String name = annotation.value();
                    if (type==null || name==null || name.trim().length()==0) {
                        return;
                    }
                    // init mq thread for each consumer
                    ConsumerHandler consumerHandler = (ConsumerHandler) serviceBean;
                    MqThread mqThread = new MqThread(consumerHandler);
                    mqThread.start();
                    mqThreadMap.put(name, mqThread);
                    logger.info(">>>>>>>>>>> xxl-mq, init consumer success, name={}, type={}, ConsumerHandler={}", name, type, serviceBean.getClass());
                }
            }
        }

    }

    // refresh registry address
    private static final int localPort = 6080;
    private static String localConsumerRegistryAddress = ZkServiceUtil.getAddress(localPort);
    private static Executor executor = Executors.newCachedThreadPool();
    static {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // registry
                    try {
                        ZkServiceUtil.registry(localPort, mqThreadMap.keySet());
                        TimeUnit.SECONDS.sleep(30);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        });
    }

    private static ConcurrentHashMap<String, MqThread> mqThreadMap = new ConcurrentHashMap<String, MqThread>();
    public static class MqThread extends Thread {

        private ConsumerHandler consumerHandler;
        public MqThread(ConsumerHandler consumerHandler) {
            this.consumerHandler = consumerHandler;
        }

        @Override
        public void run() {
            MqConsumer annotation = consumerHandler.getClass().getAnnotation(MqConsumer.class);
            MqConsumer.MqType type = annotation.type();
            final String name = annotation.value();
            if (type==null || name==null || name.trim().length()==0) {
                return;
            }
            switch (type){
                case TOPIC :{
                    // TODO
                }
                case SERIAL_QUEUE :{
                    int waitTim = 5;
                    while (true) {
                        try {
                            int[] result = ZkServiceUtil.registryRankInfo(name, localConsumerRegistryAddress);
                            if (result!=null) {
                                int consumerTotal = result[0];
                                int consumerRank = result[1];

                                if (consumerRank==0) {
                                    LinkedList<Message> list =  XxlMqClient.getXxlMqService().pullMessage(name, Message.Status.NEW.name(), 10, 0, 1);
                                    if (list!=null && list.size()>0) {
                                        waitTim = 0;
                                        for (Message msg: list) {
                                            try {
                                                consumerHandler.consume(msg);
                                                msg.setStatus(Message.Status.SUCCESS);
                                            } catch (Exception e) {
                                                logger.error("", e);
                                                msg.setStatus(Message.Status.FAIL);
                                                msg.setMsg("<hr>: " + e.getMessage());
                                            }
                                            XxlMqClient.getXxlMqService().updateMessage(msg);
                                        }
                                    } else {
                                        if (waitTim<60) {
                                            waitTim += 5;
                                        }
                                    }
                                    if (waitTim>0) {
                                        TimeUnit.SECONDS.sleep(waitTim);
                                    }

                                } else {
                                    TimeUnit.SECONDS.sleep(5);
                                }

                            } else {
                                logger.info(">>>>>>>>>>> xxl-mq, registryRankInfo(SERIAL_QUEUE) fail, registryKey:{}", name);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }case QUEUE :{
                    int waitTim = 5;
                    while (true) {
                        try {
                            int[] result = ZkServiceUtil.registryRankInfo(name, localConsumerRegistryAddress);
                            if (result!=null) {
                                int consumerTotal = result[0];
                                int consumerRank = result[1];

                                LinkedList<Message> list =  XxlMqClient.getXxlMqService().pullMessage(name, Message.Status.NEW.name(), 10, consumerRank, consumerTotal);
                                if (list!=null && list.size()>0) {
                                    waitTim = 0;
                                    for (Message msg: list) {
                                        try {
                                            consumerHandler.consume(msg);
                                            msg.setStatus(Message.Status.SUCCESS);
                                        } catch (Exception e) {
                                            logger.error("", e);
                                            msg.setStatus(Message.Status.FAIL);
                                            msg.setMsg("<hr>: " + e.getMessage());
                                        }
                                        XxlMqClient.getXxlMqService().updateMessage(msg);
                                    }
                                } else {
                                    if (waitTim<60) {
                                        waitTim += 5;
                                    }
                                }
                                if (waitTim>0) {
                                    TimeUnit.SECONDS.sleep(waitTim);
                                }
                            } else {
                                TimeUnit.SECONDS.sleep(5);
                                logger.info(">>>>>>>>>>> xxl-mq, registryRankInfo(QUEUE) fail, registryKey:{}", name);
                            }

                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(i%2);
        }
    }
}
