package com.xxl.mq.client.consumer.thread;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqResult;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.xxl.mq.client.factory.XxlMqClientFactory;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.util.LogHelper;
import com.xxl.mq.client.util.ThrowableUtil;
import com.xxl.rpc.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;
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

        this.uuid = UUID.randomUUID().toString().replaceAll("-", "");
    }

    public MqConsumer getMqConsumer() {
        return mqConsumer;
    }
    public String getUuid() {
        return uuid;
    }

    @Override
    public void run() {

        int waitTim = 0;

        while (!XxlMqClientFactory.clientFactoryPoolStoped) {
            try {
                // check active
                ConsumerRegistryHelper.ActiveInfo activeInfo = XxlMqClientFactory.getConsumerRegistryHelper().isActice(this);
                logger.debug(">>>>>>>>>>> xxl-mq, consumer active check, topic:{}, group:{}, ActiveInfo={}", mqConsumer.topic(), mqConsumer.group(), activeInfo);

                if (activeInfo != null) {

                    // pullNewMessage
                    List<XxlMqMessage> messageList = XxlMqClientFactory.getXxlMqBroker().pullNewMessage(mqConsumer.topic(), mqConsumer.group(), activeInfo.rank, activeInfo.total, 100);
                    if (messageList != null && messageList.size() > 0) {

                        // reset wait time
                        if (mqConsumer.transaction()) {
                            waitTim = 0;    // transaction message status timely updated by lock, will not repeat pull
                        } else {
                            waitTim = 1;    // no-transaction message status delay updated by callback, may be repeat, need wail for callback
                        }

                        for (final XxlMqMessage msg : messageList) {

                            // check active twice
                            ConsumerRegistryHelper.ActiveInfo newActiveInfo = XxlMqClientFactory.getConsumerRegistryHelper().isActice(this);
                            if (!(newActiveInfo != null && newActiveInfo.rank == activeInfo.rank && newActiveInfo.total == activeInfo.total)) {
                                break;
                            }

                            // lock message, for transaction
                            if (mqConsumer.transaction()) {
                                String appendLog_lock = LogHelper.makeLog(
                                        "锁定消息",
                                        ("消费者信息="+newActiveInfo.toString()
                                                +"；<br>消费者IP="+IpUtil.getIp())
                                );
                                int lockRet = XxlMqClientFactory.getXxlMqBroker().lockMessage(msg.getId(), appendLog_lock);
                                if (lockRet < 1) {
                                    continue;
                                }
                            }

                            // consume message
                            MqResult mqResult = null;
                            try {

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
                                String errorMsg = ThrowableUtil.toString(e);
                                mqResult = new MqResult(MqResult.FAIL_CODE, errorMsg);
                            }

                            // log
                            String appendLog_consume = null;
                            if (mqConsumer.transaction()) {
                                appendLog_consume = LogHelper.makeLog(
                                        "消费消息",
                                        ("消费结果="+(mqResult.isSuccess()?"成功":"失败")
                                                +"；<br>消费日志="+mqResult.getLog())
                                );
                            } else {
                                appendLog_consume = LogHelper.makeLog(
                                        "消费消息",
                                        ("消费结果="+(mqResult.isSuccess()?"成功":"失败")
                                                +"；<br>消费者信息="+activeInfo.toString()
                                                +"；<br>消费者IP="+IpUtil.getIp()
                                                +"；<br>消费日志="+mqResult.getLog())
                                );
                            }

                            // callback
                            msg.setStatus(mqResult.isSuccess()? XxlMqMessageStatus.SUCCESS.name():XxlMqMessageStatus.FAIL.name());
                            msg.setLog(appendLog_consume);
                            XxlMqClientFactory.callbackMessage(msg);

                            logger.info(">>>>>>>>>>> xxl-mq, consumer finish,  topic:{}, group:{}, ActiveInfo={}", mqConsumer.topic(), mqConsumer.group(), activeInfo.toString());
                        }

                    } else {
                        waitTim = (waitTim+10)<=60?(waitTim+10):60;
                    }
                } else {
                    waitTim = 2;
                }

            } catch (Exception e) {
                if (!XxlMqClientFactory.clientFactoryPoolStoped) {
                    logger.error(e.getMessage(), e);
                }
            }

            // wait
            try {
                TimeUnit.SECONDS.sleep(waitTim);
            } catch (Exception e) {
                if (!XxlMqClientFactory.clientFactoryPoolStoped) {
                    logger.error(e.getMessage(), e);
                }
            }

        }
    }
}
