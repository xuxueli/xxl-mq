package com.xxl.mq.sample.frameless.mqconsumer;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqResult;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 16/8/28.
 */
@MqConsumer(topic = "topic_2")
public class Demo2MqComsumer implements IMqConsumer {
    private Logger logger = LoggerFactory.getLogger(Demo2MqComsumer.class);

    @Override
    public MqResult consume(String data) throws Exception {
        logger.info("[Demo2MqComsumer] 消费一条消息:{}", data);
        return MqResult.SUCCESS;
    }

}
