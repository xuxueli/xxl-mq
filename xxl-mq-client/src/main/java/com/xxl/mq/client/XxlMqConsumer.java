package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.service.ConsumerHandler;
import com.xxl.mq.client.service.annotation.MqConsumer;
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
public class XxlMqConsumer  implements ApplicationContextAware {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqConsumer.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(MqConsumer.class);
        if (serviceMap!=null && serviceMap.size()>0) {
            for (Object serviceBean : serviceMap.values()) {
                if (serviceBean instanceof ConsumerHandler) {
                    consumerInit((ConsumerHandler) serviceBean);
                }
            }
        }

    }

    private static Executor executor = Executors.newCachedThreadPool();
    private static void consumerInit(final ConsumerHandler consumerHandler){
        MqConsumer annotation = consumerHandler.getClass().getAnnotation(MqConsumer.class);
        MqConsumer.MqType type = annotation.type();
        final String name = annotation.value();
        if (type==null || name==null || name.trim().length()==0) {
            return;
        }
        switch (type){
            case TOPIC :{
                executor.execute(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
            case SERIAL_QUEUE :{
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            LinkedList<Message> list =  XxlMqClient.getXxlMqService().pageList(1, name);
                            if (list!=null && list.size()>0) {
                                for (Message msg: list) {
                                    //ZkServiceUtil.registry();
                                    consumerHandler.consume(msg);
                                }
                            } else {
                                try {
                                    TimeUnit.SECONDS.sleep(5);
                                } catch (InterruptedException e) {
                                    logger.error("", e);
                                }
                            }
                        }
                    }
                });
            }case QUEUE :{
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            try {
                                LinkedList<Message> list =  XxlMqClient.getXxlMqService().pageList(1, name);
                                if (list!=null && list.size()>0) {
                                    for (Message msg: list) {
                                        consumerHandler.consume(msg);
                                        msg.setStatus(Message.Status.SUCCESS);
                                        msg.setMsg("消费成功");
                                        XxlMqClient.getXxlMqService().updateMessage(msg);
                                    }
                                } else {
                                    TimeUnit.SECONDS.sleep(1);
                                }
                            } catch (Exception e) {
                                logger.error("", e);
                            }
                        }
                    }
                });
            }
        }
    }

}
