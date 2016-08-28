package com.xxl.mq.example.mqcomsumer;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.util.JacksonUtil;
import com.xxl.mq.client.service.ConsumerHandler;
import com.xxl.mq.client.service.annotation.MqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by xuxueli on 16/8/28.
 */
@MqConsumer("test")
@Service
public class DemoMqComsumer implements ConsumerHandler {
    private Logger logger = LoggerFactory.getLogger(DemoMqComsumer.class);

    @Override
    public void consume(Message message) {
        logger.info("消费一条消息:{}", JacksonUtil.writeValueAsString(message));
    }

}
