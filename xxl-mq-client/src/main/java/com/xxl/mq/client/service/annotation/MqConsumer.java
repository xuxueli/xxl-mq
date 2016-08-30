package com.xxl.mq.client.service.annotation;

import java.lang.annotation.*;

/**
 * Created by xuxueli on 16/8/28.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MqConsumer {

    /**
     * 主题
     * @return
     */
    String value();

    /**
     * 类型
     * @return
     */
    MqType type() default MqType.QUEUE;

    enum MqType{
        /**
         * 广播消息
         */
        TOPIC,
        /**
         * 串行消费队列
         */
        SERIAL_QUEUE,
        /**
         * 并行消费队列
         */
        QUEUE
    }

}
