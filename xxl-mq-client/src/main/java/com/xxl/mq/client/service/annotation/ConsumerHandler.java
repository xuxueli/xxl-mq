package com.xxl.mq.client.service.annotation;

/**
 * Created by xuxueli on 16/8/28.
 */
public @interface ConsumerHandler {

    /**
     * 和Message的name属性对应,用于匹配消息队列
     * @return
     */
    String value();

}
