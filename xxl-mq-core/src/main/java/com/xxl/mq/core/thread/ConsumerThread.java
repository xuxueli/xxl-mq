package com.xxl.mq.core.thread;

import com.xxl.mq.core.XxlMqHelper;
import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.consumer.IConsumer;
import com.xxl.mq.core.context.XxlMqContext;
import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.mq.core.util.ConsumeLogUtil;
import com.xxl.tool.http.IPTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * consumer
 *
 * Created by xuxueli on 16/8/28.
 */
public class ConsumerThread {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerThread.class);

    private final XxlMqBootstrap xxlMqBootstrap;
    private final IConsumer consumer;
    private final ScheduledThreadPoolExecutor scheduledExecutorService;
    private volatile long lastExecuteTime;

    public ConsumerThread(XxlMqBootstrap xxlMqBootstrap, IConsumer consumer) {
        this.xxlMqBootstrap = xxlMqBootstrap;
        this.consumer = consumer;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        this.lastExecuteTime = System.currentTimeMillis();
    }

    public void stop() {
        try {
            // 1. mark shutdown, reject new task
            scheduledExecutorService.shutdown();
            // 2、wait stop for 15s
            if (!scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                // 3、force shutdownNow
                scheduledExecutorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 5、error，force stop
            /*List<Runnable> unfinishedTasks = scheduledExecutorService.shutdownNow();
            Thread.currentThread().interrupt();*/
            scheduledExecutorService.shutdownNow();
        }
    }

    /**
     * submit new task
     *
     * @param message
     */
    public void accept(MessageData message) {
        scheduledExecutorService.schedule(
                new Runnable() {
                    @Override
                    public void run() {
                        // refresh execute time
                        lastExecuteTime = System.currentTimeMillis();

                        // process if shutdown
                        if (scheduledExecutorService.isShutdown()) {

                            // do callback
                            String consumeLog = ConsumeLogUtil.BR_TAG + "consumer-thread terminated, the message not consumed and message-status change to：" + 0;
                            xxlMqBootstrap.getMessageThread().consumeCallback(
                                    new MessageData(
                                            message.getId(),
                                            message.getTopic(),
                                            0,      // not executed, renew orignal(0) status
                                            consumeLog,
                                            null
                                    )
                            );
                            return;
                        }

                        // set context
                        XxlMqContext.setContext(new XxlMqContext(message.getId(), message.getData()));

                        // consume
                        try {
                            int executeTimeout = 0;     // todo；
                            if (executeTimeout > 0) {
                                // limit timeout
                                Thread futureThread = null;
                                try {
                                    FutureTask<Boolean> futureTask = new FutureTask<>(() -> {

                                        // reset context
                                        XxlMqContext.setContext(new XxlMqContext(message.getId(), message.getData()));

                                        consumer.consume();
                                        return true;
                                    });
                                    futureThread = new Thread(futureTask);
                                    futureThread.start();

                                    Boolean tempResult = futureTask.get(executeTimeout, TimeUnit.SECONDS);
                                } catch (TimeoutException e) {

                                    // fill result
                                    XxlMqHelper.consumeTimeout("consume timeout.");
                                } finally {
                                    futureThread.interrupt();
                                }
                            } else {
                                // just execute
                                consumer.consume();
                            }
                        } catch (Exception e) {
                            logger.error(">>>>>>>>>>> ConsumerThread consume error, message:{}", message, e);

                            // fill result
                            XxlMqHelper.consumeFail("consume error: "+ e.getMessage());
                        } finally {

                            // cut biz log
                            String consumeLog = XxlMqContext.getContext().getConsumeLog();
                            consumeLog = (consumeLog!=null&&consumeLog.length()>500) ? (consumeLog.substring(0, 500) + "...") : consumeLog;

                            // append other log
                            consumeLog += ConsumeLogUtil.BR_TAG
                                    +"Other: IP = " + IPTool.getIp() + ", instanceUuid = "+ xxlMqBootstrap.getInstanceUuid() +" , message-status = " + message.getStatus();

                            // do callback
                            xxlMqBootstrap.getMessageThread().consumeCallback(
                                    new MessageData(
                                            message.getId(),
                                            message.getTopic(),
                                            XxlMqContext.getContext().getStatus(),
                                            consumeLog,
                                            xxlMqBootstrap.getInstanceUuid()
                                    )
                            );
                        }
                    }
                },
                message.getEffectTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * is busy (if too busy, stop consuming messages)
     *
     * @return
     */
    public boolean isBusy() {
        return scheduledExecutorService.getQueue().size() > 10;
    }

    /**
     * detect is idle (if idel, stop thread.)
     */
    public boolean isIdle() {
        if (isBusy()) {
            return false;
        }
        return (System.currentTimeMillis() - lastExecuteTime) > 3 * 60 * 1000;      // idel 3min
    }

}