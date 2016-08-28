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
     * 和Message的name属性对应,用于匹配消息队列
     * @return
     */
    String value();

}
