package com.xxl.mq.client.consumer.annotation;

import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;

import java.lang.annotation.*;

/**
 * Created by xuxueli on 16/8/28.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MqConsumer {

    /**
     * @return
     */
    String group() default ConsumerRegistryHelper.DEFAULT_GROUP;

    /**
     * @return
     */
    String topic();

}
