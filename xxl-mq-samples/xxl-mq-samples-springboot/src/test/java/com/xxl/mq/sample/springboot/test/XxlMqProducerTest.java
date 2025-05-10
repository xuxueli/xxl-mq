package com.xxl.mq.sample.springboot.test;

import com.xxl.mq.core.XxlMqHelper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class XxlMqProducerTest {

    @Test
    public void test() throws InterruptedException {

        int msgNum = 10000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < msgNum; i++) {
            XxlMqHelper.produce("topic_1", "Data:"+i);
        }
        long end = System.currentTimeMillis();
        System.out.println("msgNum = " + msgNum + ", cost = " + (end-start));

        // async send msg, wait
        TimeUnit.SECONDS.sleep(30);
    }

}
