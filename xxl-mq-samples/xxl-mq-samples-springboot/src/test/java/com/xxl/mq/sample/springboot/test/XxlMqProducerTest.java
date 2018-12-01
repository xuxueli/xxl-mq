package com.xxl.mq.sample.springboot.test;

import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.producer.XxlMqProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class XxlMqProducerTest {

    @Test
    public void test() throws InterruptedException {

        int msgNum = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < msgNum; i++) {
            XxlMqProducer.produce(new XxlMqMessage("topic_1", "Data:"+i));
        }
        long end = System.currentTimeMillis();
        System.out.println("msgNum = " + msgNum + ", cost = " + (end-start));

        // async send msg, wait
        TimeUnit.SECONDS.sleep(30);
    }

}
