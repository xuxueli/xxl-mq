package com.xxl.mq.sample.frameless.mqconsumer;

import com.xxl.mq.core.XxlMqHelper;
import com.xxl.mq.core.consumer.annotation.XxlMq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XxlMq开发示例
 *
 * 开发步骤：
 *      1、任务开发：新增 MQ消费者方法
 *      2、注解配置：为 MQ消费者方法 添加注解 "@XxlMq(value="消费者 topic ", init = "消费者初始化方法", destroy = "消费者销毁方法")"，注解value值对应的是消息中心新建消息主题的 Topic 值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
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

}
