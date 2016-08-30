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
@MqConsumer("mqconsumer-01")
@Service
public class DemoAMqComsumer implements ConsumerHandler {
    private Logger logger = LoggerFactory.getLogger(DemoAMqComsumer.class);

    @Override
    public void consume(Message message) throws Exception {
        logger.info("{}消费一条消息:{}", "mqconsumer-01",  JacksonUtil.writeValueAsString(message));
    }

}
