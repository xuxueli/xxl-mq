package com.xxl.mq.sample.springboot.mqcomsumer;

import com.xxl.mq.core.XxlMqHelper;
import com.xxl.mq.core.consumer.annotation.XxlMq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by xuxueli on 16/8/28.
 */
@Service
public class SimpleXxlMq {
    private Logger logger = LoggerFactory.getLogger(SimpleXxlMq.class);

    @XxlMq("topic_sample")
    public void consume() {
        // consume message
        String messageData = XxlMqHelper.getMessageData();
        logger.info("topic[{}] consume message: {}", "topic_sample", messageData);

        // set consume result
        XxlMqHelper.consumeSuccess();
    }

    @XxlMq("topic_sample_02")
    public void consume02() {
        // consume message
        String messageData = XxlMqHelper.getMessageData();
        logger.info("topic[{}] consume message: {}", "topic_sample_02", messageData);

        // set consume result
        XxlMqHelper.consumeFail("consume fail.");
    }

}