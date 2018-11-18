package com.xxl.mq.client.consumer.thread;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqResult;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.util.DateFormatUtil;
import com.xxl.rpc.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by xuxueli on 16/9/10.
 */
public class ConsumerThread extends Thread {
    private final static Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

    private IMqConsumer consumerHandler;
    private MqConsumer mqConsumer;

    private String uuid;

    public ConsumerThread(IMqConsumer consumerHandler) {
        this.consumerHandler = consumerHandler;
        this.mqConsumer = consumerHandler.getClass().getAnnotation(MqConsumer.class);

        uuid = IpUtil.getIp().concat("_").concat(String.valueOf(System.currentTimeMillis())).concat("_").concat(String.valueOf(this.hashCode()));
    }

    public MqConsumer getMqConsumer() {
        return mqConsumer;
    }
    public String getUuid() {
        return uuid;
    }

    @Override
    public void run() {

        int waitTim = 5;

        while (!XxlMqClientFactory.clientFactoryPoolStoped) {
            try {
                // check active
                ConsumerRegistryHelper.ActiveInfo activeInfo = XxlMqClientFactory.getConsumerRegistryHelper().isActice(this);
                logger.info(">>>>>>>>>>> xxl-mq, consumer active check, topic:{}, group:{}, ActiveInfo={}", mqConsumer.topic(), mqConsumer.group(), activeInfo.toString());

                if (activeInfo != null) {

                    // pullNewMessage
                    List<XxlMqMessage> messageList = XxlMqClientFactory.getXxlMqBroker().pullNewMessage(mqConsumer.topic(), mqConsumer.group(), activeInfo.rank, activeInfo.total, 100);
                    if (messageList != null && messageList.size() > 0) {
                        waitTim = 0;

                        for (final XxlMqMessage msg : messageList) {

                            // check active twice
                            ConsumerRegistryHelper.ActiveInfo newActiveInfo = XxlMqClientFactory.getConsumerRegistryHelper().isActice(this);
                            if (!(newActiveInfo != null && newActiveInfo.rank == activeInfo.rank && newActiveInfo.total == activeInfo.total)) {
                                break;
                            }

                            // lock message
                            String appendLog_lock = "<hr>》》》时间: "+ DateFormatUtil.getNowTime() +" <br>》》》操作: 消息锁定<br>》》》注册信息: " + newActiveInfo.toString();
                            int lockRet = XxlMqClientFactory.getXxlMqBroker().lockMessage(msg.getId(), appendLog_lock);
                            if (lockRet < 1) {
                                continue;
                            }

                            MqResult mqResult = null;
                            try {
                                // consume data

                                if (msg.getTimeout() > 0) {
                                    // limit timeout
                                    Thread futureThread = null;
                                    try {
                                        FutureTask<MqResult> futureTask = new FutureTask<MqResult>(new Callable<MqResult>() {
                                            @Override
                                            public MqResult call() throws Exception {
                                                return consumerHandler.consume(msg.getData());
                                            }
                                        });
                                        futureThread = new Thread(futureTask);
                                        futureThread.start();

                                        mqResult = futureTask.get(msg.getTimeout(), TimeUnit.SECONDS);
                                    } catch (TimeoutException e) {
                                        logger.error(e.getMessage(), e);
                                        mqResult = new MqResult(MqResult.FAIL_CODE, "Timeout:" + e.getMessage());
                                    } finally {
                                        futureThread.interrupt();
                                    }
                                } else {
                                    // direct run
                                    mqResult = consumerHandler.consume(msg.getData());
                                }

                                if (mqResult == null) {
                                    mqResult = MqResult.FAIL;
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                                mqResult = new MqResult(MqResult.FAIL_CODE, e.getMessage());
                            }
                            String appendLog_consume = "<hr>》》》时间: "+ DateFormatUtil.getNowTime() +" <br>》》》操作: 消息消费"+ (mqResult.isSuccess()?"成功":"失败") +"<br>》》》<br>》》》日志:"+mqResult.getLog();

                            // callback
                            msg.setStatus(mqResult.isSuccess()? XxlMqMessageStatus.SUCCESS.name():XxlMqMessageStatus.FAIL.name());
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
