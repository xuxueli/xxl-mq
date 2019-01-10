package com.xxl.mq.client.consumer.annotation;

import java.lang.annotation.*;

/**
 * Created by xuxueli on 16/8/28.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MqConsumer {

    public static final String DEFAULT_GROUP = "DEFAULT";   // default group
    public static final String EMPTY_GROUP = "";            // empty group means consume broadcase message, will replace by uuid

    /**
     * @return
     */
    String group() default DEFAULT_GROUP;

    /**
     * @return
     */
    String topic();

    /**
     * @return
     */
    boolean transaction() default true;

}
