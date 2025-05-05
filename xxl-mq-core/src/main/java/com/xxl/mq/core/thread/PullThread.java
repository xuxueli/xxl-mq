package com.xxl.mq.core.thread;

import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.mq.core.openapi.model.PullRequest;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * pull thread
 *
 * Created by xuxueli on 16/8/28.
 */
public class PullThread {
    private static final Logger logger = LoggerFactory.getLogger(PullThread.class);

    public static final int PULL_INTERVAL = 10 * 1000;

    private final XxlMqBootstrap xxlMqBootstrap;
    public PullThread(final XxlMqBootstrap xxlMqBootstrap) {
        this.xxlMqBootstrap = xxlMqBootstrap;
    }

    public void start() {

        CyclicThread pullThread = new CyclicThread(
                "pullThread",
                new Runnable() {
                    @Override
                    public void run() {

                        // find idel consumer, stop them
                        xxlMqBootstrap.stopIdleConsumerThead();

                        // exclude busy consumer, pass if all-busy
                        if (CollectionTool.isEmpty(xxlMqBootstrap.getFreeConsumerTopicList())) {
                            return;
                        }

                        // request
                        PullRequest pullRequest = new PullRequest();
                        pullRequest.setAccesstoken(xxlMqBootstrap.getAccesstoken());
                        pullRequest.setAppname(xxlMqBootstrap.getAppname());
                        pullRequest.setInstanceUuid(xxlMqBootstrap.getInstanceUuid());
                        pullRequest.setTopicList(xxlMqBootstrap.getFreeConsumerTopicList());

                        // invoke
                        Response<List<MessageData>> pullResponse = xxlMqBootstrap.loadBrokerClient().pull(pullRequest);
                        if (!pullResponse.isSuccess()) {
                            logger.error(">>>>>>>>>>> xxl-mq PullThread-pullThread pull fail, pullRequest:{}, pullResponse:{}", pullRequest, pullResponse);
                            return;
                        }

                        // process
                        List<MessageData> messageDataList = pullResponse.getData();
                        if (CollectionTool.isNotEmpty(messageDataList)) {
                            for (MessageData messageData : messageDataList) {
                                try {
                                    // lazy init consumer-thread
                                    ConsumerThread consumerThread = xxlMqBootstrap.lazyInitConsumerThread(messageData.getTopic());
                                    // accept message
                                    consumerThread.accept(messageData);
                                } catch (Exception e) {
                                    logger.error(">>>>>>>>>>> xxl-mq PullThread message-accept error, messageData:{}", messageData, e);
                                }
                            }
                        }

                    }
                },
                PULL_INTERVAL,
                true);
        pullThread.start();

    }

    public void stop() {
        // do something
    }

}