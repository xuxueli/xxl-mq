package com.xxl.mq.core.consumer.annotation;

import java.lang.annotation.*;

/**
 * xxl-mq consumer annotation
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XxlMq {

    /**
     * topic name
     */
    String value();

    /**
     * init method, invoked when IConsumer init
     */
    String init() default "";

    /**
     * destroy method, invoked when IConsumer destroy
     */
    String destroy() default "";

}