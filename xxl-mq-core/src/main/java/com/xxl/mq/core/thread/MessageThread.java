package com.xxl.mq.core.thread;

import com.xxl.mq.core.bootstrap.XxlMqBootstrap;
import com.xxl.mq.core.openapi.model.ConsumeRequest;
import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.mq.core.openapi.model.ProduceRequest;
import com.xxl.tool.concurrent.MessageQueue;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.exception.BizException;
import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * producer thread
 *
 * Created by xuxueli on 16/8/28.
 */
public class MessageThread {
    private static final Logger logger = LoggerFactory.getLogger(MessageThread.class);


    // ---------------------- init ----------------------

    private final XxlMqBootstrap xxlMqBootstrap;

    public MessageThread(final XxlMqBootstrap xxlMqBootstrap) {
        this.xxlMqBootstrap = xxlMqBootstrap;
    }


    // ---------------------- start / stop ----------------------

    private volatile MessageQueue<MessageData> produceMessageQueue;
    private volatile MessageQueue<MessageData> consumeMessageQueue;

    /**
     * start
     */
    public void start() {
        // produce messages (new message)
        produceMessageQueue = new MessageQueue<MessageData>(
                "produceMessageQueue",
                messages -> {

                    // param
                    ProduceRequest produceRequest = new ProduceRequest();
                    produceRequest.setAccesstoken(xxlMqBootstrap.getAccesstoken());
                    produceRequest.setMessageList(messages);

                    // retry 3 times
                    for (int i = 0; i < 3; i++) {
                        try {
                            Response<String> produceResponse = xxlMqBootstrap.loadBrokerClient().produce(produceRequest);
                            if (produceResponse.isSuccess()) {
                                logger.debug(">>>>>>>>>>> xxl-mq MessageThread-produceMessageQueue produce success, produceRequest:{}, produceResponse:{}", produceRequest, produceResponse);
                                break;
                            } else {
                                logger.error(">>>>>>>>>>> xxl-mq MessageThread-produceMessageQueue produce fail, produceRequest:{}, produceResponse:{}", produceRequest, produceResponse);
                            }
                        } catch (Exception e) {
                            logger.error(">>>>>>>>>>> xxl-mq MessageThread-produceMessageQueue produce error, produceRequest:{}", produceRequest, e);
                        }
                    }
                },
                5,
                50);

        // consume messages (callback message status)
        consumeMessageQueue = new MessageQueue<MessageData>(
                "consumeMessageQueue",
                messages -> {

                    // param
                    ConsumeRequest consumeRequest = new ConsumeRequest();
                    consumeRequest.setAccesstoken(xxlMqBootstrap.getAccesstoken());
                    consumeRequest.setMessageList(messages);

                    // retry 3 times
                    for (int i = 0; i < 3; i++) {
                        try {
                            Response<String> consumeResponse = xxlMqBootstrap.loadBrokerClient().consume(consumeRequest);
                            if (consumeResponse.isSuccess()) {
                                logger.debug(">>>>>>>>>>> xxl-mq MessageThread-consumeMessageQueue consume success, consumeRequest:{}, consumeResponse:{}", consumeRequest, consumeResponse);
                                break;
                            } else {
                                logger.error(">>>>>>>>>>> xxl-mq MessageThread-consumeMessageQueue consume fail, consumeRequest:{}, consumeResponse:{}", consumeRequest, consumeResponse);
                            }
                        } catch (Exception e) {
                            logger.error(">>>>>>>>>>> xxl-mq MessageThread-consumeMessageQueue consume error, consumeRequest:{}", consumeRequest, e);
                        }
                    }
                },
                5,
                50);
    }

    /**
     * stop
     */
    public void stop() {
        // do something
        if (produceMessageQueue != null) {
            produceMessageQueue.stop();
        }
        if (consumeMessageQueue != null) {
            consumeMessageQueue.stop();
        }
    }


    // ---------------------- produce ----------------------

    /**
     * produce message
     *
     * @param messageData
     * @return
     */
    public boolean produceSend(MessageData messageData) {

        // valid
        if (messageData == null || StringTool.isBlank(messageData.getTopic())) {
            logger.error(">>>>>>>>>>> xxl-mq MessageThread-produceSend fail, message topic is null", new BizException("message topic is null"));
            return false;
        }
        if (messageData.getTopic().length() > 100) {
            logger.error(">>>>>>>>>>> xxl-mq MessageThread-produceSend fail, message topic is too long（>100): topic = {}", messageData.getTopic(), new BizException("message topic is too long"));
            return false;
        }
        if (messageData.getData()==null ) {
            messageData.setData("");
        }
        if (messageData.getData().length()>4000) {
            logger.error(">>>>>>>>>>> xxl-mq MessageThread-produceSend fail, message data is too long（>4000): topic = {}", messageData.getTopic(), new BizException("message data is too long"));
            return false;
        }
        if (messageData.getEffectTime() < System.currentTimeMillis()) {
            messageData.setEffectTime(System.currentTimeMillis());
        }
        if (messageData.getBizId() <= 0) {
            messageData.setBizId(0);
        }

        // invoke
        return produceMessageQueue.produce(messageData);
    }

    /**
     * consume message
     *
     * @param messageData
     * @return
     */
    public boolean consumeCallback(MessageData messageData) {
        return consumeMessageQueue.produce(messageData);
    }

}