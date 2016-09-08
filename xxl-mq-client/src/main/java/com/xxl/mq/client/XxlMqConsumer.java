package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.util.ZkConsumerUtil;
import com.xxl.mq.client.service.ConsumerHandler;
import com.xxl.mq.client.service.annotation.MqConsumer;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.text.MessageFormat;
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

        // init consumer respository
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
                    ConsumerThread mqThread = new ConsumerThread(consumerHandler);
                    consumerRespository.put(name, mqThread);
                }
            }
        }
        if (consumerRespository ==null || consumerRespository.size()==0) {
            return;
        }

        // registry consumer, and fresh each 60s
        try {
            ZkConsumerUtil.registerConsumers(consumerRespository.keySet());
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
                        ZkConsumerUtil.registerConsumers(consumerRespository.keySet());
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
            logger.info(">>>>>>>>>>> xxl-mq, consumer thread start, name={}, type={}", annotation.value(), annotation.type());
        }

    }

    // fresh consumer
    private static Executor executor = Executors.newCachedThreadPool();

    // consumer respository
    private static ConcurrentHashMap<String, ConsumerThread> consumerRespository = new ConcurrentHashMap<String, ConsumerThread>();

    // consumer thread
    public static class ConsumerThread extends Thread {

        private ConsumerHandler consumerHandler;
        public ConsumerThread(ConsumerHandler consumerHandler) {
            this.consumerHandler = consumerHandler;
        }
        public ConsumerHandler getConsumerHandler() {
            return consumerHandler;
        }

        @Override
        public void run() {
            MqConsumer annotation = consumerHandler.getClass().getAnnotation(MqConsumer.class);
            MqConsumer.MqType type = annotation.type();
            final String name = annotation.value();

            int pagesize = 10;
            int waitTim = 5;
            switch (type){
                case TOPIC :{
                    // TODO
                }
                case SERIAL_QUEUE :{
                    // TODO
                }case QUEUE :{

                    while (true) {
                        try {
                            // check load
                            ZkConsumerUtil.ActiveInfo checkPull = ZkConsumerUtil.isActice(annotation);
                            if (checkPull!=null) {

                                // load
                                LinkedList<Message> messageList =  XxlMqClient.getXxlMqService().pullMessage(name, Message.Status.NEW.name(), pagesize, checkPull.rank, checkPull.total);
                                if (messageList!=null && messageList.size()>0) {
                                    waitTim = 0;
                                    for (Message msg: messageList) {

                                        // check consumer
                                        ZkConsumerUtil.ActiveInfo checkConsume = ZkConsumerUtil.isActice(annotation);
                                        if (!(checkConsume!=null && checkConsume.rank==checkPull.rank && checkConsume.total==checkPull.total)) {
                                            break;
                                        }

                                        // consumer
                                        msg.setStatus(Message.Status.ING);
                                        msg.setMsg(MessageFormat.format("<hr>》》》机器: {0} <br>》》》操作: 消息锁定(status>>>ING)", ZkConsumerUtil.localAddress));
                                        XxlMqClient.getXxlMqService().updateMessage(msg);

                                        try {
                                            consumerHandler.consume(msg);
                                            msg.setStatus(Message.Status.SUCCESS);
                                            msg.setMsg(MessageFormat.format("<hr>》》》机器: {0} <br>》》》操作: 消息消费成功(status>>>SUCCESS)", ZkConsumerUtil.localAddress));
                                        } catch (Exception e) {
                                            logger.error("", e);
                                            msg.setStatus(Message.Status.FAIL);
                                            msg.setMsg(MessageFormat.format("<hr>》》》机器: {0} <br>》》》操作: 消息锁定失败(status>>>FAIL) <br>日志:{1}", ZkConsumerUtil.localAddress, e.getMessage()));
                                        } finally {
                                            XxlMqClient.getXxlMqService().updateMessage(msg);
                                        }

                                    }
                                }

                                waitTim = (waitTim<60) ? (waitTim+5) : waitTim;
                                if (waitTim>0) {
                                    TimeUnit.SECONDS.sleep(waitTim);
                                }

                            } else {
                                TimeUnit.SECONDS.sleep(5);
                                logger.info(">>>>>>>>>>> xxl-mq, isActice(QUEUE) fail, registryKey:{}", name);
                            }

                        } catch (Exception e) {
                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (Exception e1) {
                                logger.error("", e1);
                            }
                            logger.error("", e);
                        }
                    }
                }
            }
        }
    }

}
