package com.xxl.mq.example.mqcomsumer;

import com.xxl.mq.client.rpc.util.JacksonUtil;
import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.consumer.annotation.MqConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.xxl.mq.client.consumer.annotation.MqConsumer.MqType.SERIAL_QUEUE;

/**
 * Created by xuxueli on 16/8/28.
 */
@MqConsumer(value = "mqconsumer-02", type = SERIAL_QUEUE)
@Service
public class DemoBMqComsumer implements IMqConsumer {
    private Logger logger = LoggerFactory.getLogger(DemoBMqComsumer.class);

    @Override
    public void consume(Map<String, String> data) throws Exception {
        logger.info("{}消费一条消息:{}", "mqconsumer-02",  JacksonUtil.writeValueAsString(data));
    }

}
