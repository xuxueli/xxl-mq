package com.xxl.mq.core.thread;

import com.xxl.mq.core.consumer.Consumer;
import com.xxl.mq.core.openapi.model.MessageData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * consumer
 *
 * Created by xuxueli on 16/8/28.
 */
public class ConsumerThread {

    private final Consumer consumer;
    private ScheduledThreadPoolExecutor scheduledExecutorService;

    public ConsumerThread(Consumer consumer) {
        this.consumer = consumer;
        this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
    }

    public void stop() {
        List<Runnable> unfinishedTasks = new ArrayList<>();
        try {
            // 1. mark shutdown, reject new task
            scheduledExecutorService.shutdown();
            // 2、wait stop for 15s
            boolean terminated = scheduledExecutorService.awaitTermination(15, TimeUnit.SECONDS);
            if (!terminated) {
                // 3、force shutdownNow
                unfinishedTasks = scheduledExecutorService.shutdownNow();
                // 4、wait again
                scheduledExecutorService.awaitTermination(3, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            // 5、error，force stop
            unfinishedTasks = scheduledExecutorService.shutdownNow();
            //Thread.currentThread().interrupt();
        }
        System.out.println("unfinishedTasks = " + unfinishedTasks);
    }

    /**
     * submit new task
     *
     * @param message
     */
    public void accept(MessageData message) {
        scheduledExecutorService.schedule(
                new ConsumerRunable(message, consumer),
                message.getEffectTime() - System.currentTimeMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * is busy
     *
     * @return
     */
    public boolean isBusy() {
        return scheduledExecutorService.getQueue().size() > 10;
    }

}