package com.xxl.mq.sample.frameless.mqconsumer;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqResult;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 16/8/28.
 */
@MqConsumer(topic = "topic_2", transaction = false)
public class DemoMqComsumer implements IMqConsumer {
    private Logger logger = LoggerFactory.getLogger(DemoMqComsumer.class);

    @Override
    public MqResult consume(String data) throws Exception {
        logger.info("[DemoMqComsumer] 消费一条消息:{}", data);
        return MqResult.FAIL;
    }

}
