package com.xxl.mq.sample.frameless.mqconsumer;

import com.xxl.mq.core.XxlMqHelper;
import com.xxl.mq.core.consumer.annotation.XxlMq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XxlMq开发示例
 *
 * 开发步骤：
 *      1、消费者开发：新增 MQ消费者方法；
 *      2、注解配置：为 MQ消费者方法 添加注解 "@XxlMq(value="消费者 topic ", init = "消费者初始化方法", destroy = "消费者销毁方法")"，注解value值对应的是消息中心新建消息主题的 Topic 值。
 *      3、消费结果：通过 "XxlMqHelper.consumeSuccess/consumeFail" 设置消费结果，默认执行成功；
 *
 * Created by xuxueli on 16/8/28.
 */
public class SimpleXxlMq {
    private Logger logger = LoggerFactory.getLogger(SimpleXxlMq.class);

    @XxlMq("topic_sample")
    public void consume() {
        // consume message
        String messageData = XxlMqHelper.getMessageData();
        logger.info("topic[{}] consume message: {}", "topic_sample", messageData);

        // set consume result
        XxlMqHelper.consumeSuccess();
    }

    @XxlMq("topic_sample_02")
    public void consume02() {
        logger.info("topic[{}] consume message: {}", "topic_sample_02", XxlMqHelper.getMessageData());
    }

    @XxlMq("topic_sample_03")
    public void consume03() {
        logger.info("topic[{}] consume message: {}", "topic_sample_03", XxlMqHelper.getMessageData());
    }

    @XxlMq("topic_sample_04")
    public void consume04() {
        logger.info("topic[{}] consume message: {}", "topic_sample_04", XxlMqHelper.getMessageData());
    }

    @XxlMq("topic_sample_05")
    public void consume05() {
        logger.info("topic[{}] consume message: {}", "topic_sample_05", XxlMqHelper.getMessageData());
    }

    @XxlMq("topic_sample_06")
    public void consume06() {
        logger.info("topic[{}] consume message: {}", "topic_sample_06", XxlMqHelper.getMessageData());

        XxlMqHelper.consumeFail("consumer fail~");
    }

    @XxlMq("topic_sample_07")
    public void consume07() {
        logger.info("topic[{}] consume message: {}", "topic_sample_07", XxlMqHelper.getMessageData());
    }

}
