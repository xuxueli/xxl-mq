package com.xxl.mq.client.consumer.thread;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.message.MessageStatus;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.topic.TopicHelper;
import com.xxl.mq.client.util.DateFormatUtil;
import com.xxl.mq.client.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/9/10.
 */ // consumer thread
public class ConsumerThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(ConsumerThread.class);
    
    private IMqConsumer consumerHandler;

    public ConsumerThread(IMqConsumer consumerHandler) {
        this.consumerHandler = consumerHandler;
    }

    public IMqConsumer getConsumerHandler() {
        return consumerHandler;
    }

    // TODO，线程销毁优化

    @Override
    public void run() {
        MqConsumer annotation = consumerHandler.getClass().getAnnotation(MqConsumer.class);

        int pagesize = 10;
        int waitTim = 5;

        while (true) {
            try {
                // check load
                TopicHelper.ActiveInfo checkPull = TopicHelper.isActice(annotation);
                if (checkPull != null) {
                    logger.info(">>>>>>>>>>> xxl-mq, isActice: consumer={}, ActiveInfo={}", annotation, checkPull.toString());

                    // load
                    List<XxlMqMessage> messageList = XxlMqClientFactory.getXxlMqBroker().pullNewMessage(annotation.value(), pagesize, checkPull.rank, checkPull.total);
                    if (messageList != null && messageList.size() > 0) {
                        waitTim = 0;
                        for (XxlMqMessage msg : messageList) {

                            // check consumer
                            TopicHelper.ActiveInfo checkConsume = TopicHelper.isActice(annotation);
                            if (!(checkConsume != null && checkConsume.rank == checkPull.rank && checkConsume.total == checkPull.total)) {
                                break;
                            }

                            String tim = DateFormatUtil.formatDateTime(new Date());

                            // lock message
                            String lockAddMsg = MessageFormat.format("<hr>》》》时间: {0} <br>》》》操作: 消息锁定(status>>>ING)<br>》》》注册信息: {1}", tim, checkConsume.toString());
                            int lockRet = XxlMqClientFactory.getXxlMqBroker().lockMessage(msg.getId(), lockAddMsg);
                            if (lockRet < 1) {
                                continue;
                            }

                            try {
                                // consume message
                                Map<String, String> data = null;
                                if (msg.getData() != null && msg.getData().trim().length() > 0) {
                                    data = JacksonUtil.readValue(msg.getData(), Map.class);
                                }
                                consumerHandler.consume(data);

                                // consume suceess
                                msg.setStatus(MessageStatus.SUCCESS.name());
                                msg.setMsg(MessageFormat.format("<hr>》》》时间: {0} <br>》》》操作: 消息消费成功(status>>>SUCCESS) <br>》》》注册信息: {1}", tim, checkConsume.toString()));
                            } catch (Exception e) {
                                logger.error("", e);

                                // consume error
                                msg.setStatus(MessageStatus.FAIL.name());
                                msg.setMsg(MessageFormat.format("<hr>》》》时间: {0} <br>》》》操作: 消息消费失败(status>>>FAIL) <br>》》》注册信息: {1} <br>》》》日志:{2}", tim, checkConsume.toString(), e.getMessage()));
                            } finally {
                                XxlMqClientFactory.getXxlMqBroker().consumeCallbackMessage(msg);
                                logger.info(">>>>>>>>>> xxl-mq, consumer message: {}", msg);
                            }

                        }
                    }

                    waitTim = (waitTim < 60) ? (waitTim + 5) : waitTim;
                    if (waitTim > 0) {
                        TimeUnit.SECONDS.sleep(waitTim);
                    }

                } else {
                    TimeUnit.SECONDS.sleep(5);
                    logger.debug(">>>>>>>>>>> xxl-mq, isActice(QUEUE) fail, registryKey:{}", annotation.value());
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
