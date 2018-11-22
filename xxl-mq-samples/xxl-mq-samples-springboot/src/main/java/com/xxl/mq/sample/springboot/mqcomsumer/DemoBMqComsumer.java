package com.xxl.mq.sample.springboot.mqcomsumer;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.MqResult;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by xuxueli on 16/8/28.
 */
@MqConsumer(topic = "topic_1", transaction = false)
@Service
public class DemoBMqComsumer implements IMqConsumer {
    private Logger logger = LoggerFactory.getLogger(DemoBMqComsumer.class);

    @Override
    public MqResult consume(String data) throws Exception {
        logger.info("[DemoBMqComsumer] 消费一条消息:{}", data);
        return MqResult.SUCCESS;
    }

}
