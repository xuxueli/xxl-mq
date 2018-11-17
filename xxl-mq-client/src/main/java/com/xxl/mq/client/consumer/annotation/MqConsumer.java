package com.xxl.mq.client.consumer.annotation;

import com.xxl.mq.client.topic.TopicHelper;

import java.lang.annotation.*;

/**
 * Created by xuxueli on 16/8/28.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MqConsumer {

    /**
     *
     * @return
     */
    String group() default TopicHelper.DEFAULT_GROUP;

    /**
     * 主题
     * @return
     */
    String value();

}
