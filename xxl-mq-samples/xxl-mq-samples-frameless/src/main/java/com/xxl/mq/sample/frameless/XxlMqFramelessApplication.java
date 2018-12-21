package com.xxl.mq.sample.frameless;


import com.xxl.mq.client.consumer.IMqConsumer;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.producer.XxlMqProducer;
import com.xxl.mq.sample.frameless.conf.XxlMqConf;
import com.xxl.mq.sample.frameless.mqconsumer.Demo2MqComsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2018-11-21
 */
public class XxlMqFramelessApplication {

    public static void main(String[] args) throws Exception {

        // consumer list
        List<IMqConsumer> consumerList = new ArrayList<>();
        consumerList.add(new Demo2MqComsumer());


        // start
        XxlMqConf.getInstance().start(consumerList);

        // producer
        XxlMqProducer.produce(new XxlMqMessage("topic_2", "test msg data"));

        while (!Thread.currentThread().isInterrupted()) {
            TimeUnit.HOURS.sleep(1);
        }

        // stop
        XxlMqConf.getInstance().stop();

    }

}
