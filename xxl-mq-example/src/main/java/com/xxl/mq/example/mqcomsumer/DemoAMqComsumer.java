package com.xxl.mq.example.mqcomsumer;

import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
@MqConsumer("mqconsumer-01")
@Service
public class DemoAMqComsumer implements IMqConsumer {
    private Logger logger = LoggerFactory.getLogger(DemoAMqComsumer.class);

    @Override
    public void consume(Map<String, String> data) throws Exception {
        logger.info("{}消费一条消息:{}", "mqconsumer-01",  data);
    }

}
