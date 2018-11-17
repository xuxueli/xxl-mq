package com.xxl.mq.client.consumer.thread;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqResult;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.xxl.mq.client.util.DateFormatUtil;
import com.xxl.mq.client.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/9/10.
 */
public class ConsumerThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

    private IMqConsumer consumerHandler;
    private MqConsumer mqConsumer;

    public ConsumerThread(IMqConsumer consumerHandler) {
        this.consumerHandler = consumerHandler;
        MqConsumer annotation = consumerHandler.getClass().getAnnotation(MqConsumer.class);
    }

    public MqConsumer getMqConsumer() {
        return mqConsumer;
    }

    @Override
    public void run() {

        int waitTim = 5;

        while (XxlMqClientFactory.consumerExecutorStoped) {
            try {
                // check active
                ConsumerRegistryHelper.ActiveInfo activeInfo = ConsumerRegistryHelper.isActice(mqConsumer);
                logger.info(">>>>>>>>>>> xxl-mq, consumer active check, topic:{}, group:{}, ActiveInfo={}", mqConsumer.topic(), mqConsumer.group(), activeInfo.toString());

                if (activeInfo != null) {

                    // pullNewMessage
                    List<XxlMqMessage> messageList = XxlMqClientFactory.getXxlMqBroker().pullNewMessage(mqConsumer.topic(), mqConsumer.group(), activeInfo.rank, activeInfo.total, 50);
                    if (messageList != null && messageList.size() > 0) {
                        waitTim = 0;

                        for (XxlMqMessage msg : messageList) {

                            // check active twice
                            ConsumerRegistryHelper.ActiveInfo newActiveInfo = ConsumerRegistryHelper.isActice(mqConsumer);
                            if (!(newActiveInfo != null && newActiveInfo.rank == activeInfo.rank && newActiveInfo.total == activeInfo.total)) {
                                break;
                            }

                            String tim = DateFormatUtil.formatDateTime(new Date());

                            // lock message
                            String appendLog_lock = MessageFormat.format("<hr>》》》时间: {0} <br>》》》操作: 消息锁定<br>》》》注册信息: {1}", tim, newActiveInfo.toString());
                            int lockRet = XxlMqClientFactory.getXxlMqBroker().lockMessage(msg.getId(), appendLog_lock);
                            if (lockRet < 1) {
                                continue;
                            }

                            MqResult mqResult = null;
                            try {
                                // consume data
                                Map<String, String> data = JacksonUtil.readValue(msg.getData(), Map.class);
                                mqResult = consumerHandler.consume(data);
                                if (mqResult == null) {
                                    mqResult = MqResult.FAIL;
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                mqResult = new MqResult(MqResult.FAIL_CODE, e.getMessage());
                            }
                            String appendLog_consume = MessageFormat.format("<hr>》》》时间: {0} <br>》》》操作: 消息消费{1}<br>》》》<br>》》》日志:{2}",
                                    tim, (mqResult.isSuccess()?"成功":"失败"), mqResult.getLog());

                            // callback
                            msg.setLog(appendLog_consume);
                            XxlMqClientFactory.getXxlMqBroker().callbackMessages(Arrays.asList(msg));

                            logger.info(">>>>>>>>>>> xxl-mq, consumer finish,  topic:{}, group:{}, ActiveInfo={}", mqConsumer.topic(), mqConsumer.group(), activeInfo.toString());
                        }
                    } else {
                        waitTim = (waitTim < 60) ? (waitTim + 10) : waitTim;
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            // wait
            try {
                TimeUnit.SECONDS.sleep(waitTim);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }
}
