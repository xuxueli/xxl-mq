package com.xxl.mq.client.consumer.annotation;

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
    MqConsumerType type() default MqConsumerType.QUEUE;

}
