package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerFactory;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.tool.concurrent.CyclicThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * ArchiveThreadHelper
 *
 * @author xuxueli
 */
public class ArchiveThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveThreadHelper.class);

    // ---------------------- init ----------------------

    private final BrokerFactory brokerFactory;
    public ArchiveThreadHelper(BrokerFactory brokerFactory) {
        this.brokerFactory = brokerFactory;
    }

    // ---------------------- start / stop ----------------------


    /**
     * start ArchiveThreadHelper (will stop with jvm)
     *
     * remark：
     *      1、archive message：
     *      2、refresh messge-report：
     *      3、alarm by topic：fail>0, by topic
     */
    public void start(){

        CyclicThread archiveThread = new CyclicThread("archiveThread", true, new Runnable() {
            @Override
            public void run() {
                // TODO
                // 1、
                List<Topic> topicList = brokerFactory.getLocalCacheThreadHelper().findTopicAll();
                for (Topic topic : topicList) {
                    brokerFactory.getMessageService().archive(topic.getTopic(), topic.getArchiveStrategy(), 10000);
                }


                // 2、

                // 3、
            }
        }, 60 * 1000, true);
        archiveThread.start();
    }

    public void stop(){
        // do nothing
    }

}
