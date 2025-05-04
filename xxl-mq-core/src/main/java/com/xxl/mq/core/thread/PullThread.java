package com.xxl.mq.core.thread;

import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.mq.core.openapi.model.PullRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * pull thread
 *
 * Created by xuxueli on 16/8/28.
 */
public class PullThread {

    private volatile boolean running = false;
    private Thread pullThread = null;

    private final XxlMqBootstrap xxlMqBootstrap;

    public PullThread(final XxlMqBootstrap xxlMqBootstrap) {
        this.xxlMqBootstrap = xxlMqBootstrap;
    }

    public void start() {
        running = true;
        pullThread = new Thread(() -> {
            while (running) {

                PullRequest pullRequest = new PullRequest();
                pullRequest.setAccesstoken(xxlMqBootstrap.getAccesstoken());
                pullRequest.setTopicList(xxlMqBootstrap.getFreeConsumers());

                // todo

                /*List<MessageData> msgList = xxlMqBootstrap.loadBrokerClient().pull(pullRequest);
                //System.out.println("pull running , FreeConsumers = " + bootstrap.getFreeConsumers());
                if (msgList != null && !msgList.isEmpty()) {
                    for (MessageData msg : msgList) {
                        ConsumerThread consumer = xxlMqBootstrap.getConsumer(msg.getTopic());
                        if (consumer == null) {
                            System.out.println("topic not found, msg = " + msg);
                            continue;
                        }
                        consumer.accept(msg);
                        ;
                    }
                }*/
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        });
        pullThread.start();
    }

    public void stop() {
        running = false;
    }
}