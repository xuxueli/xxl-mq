package com.xxl.mq.core.consumer;

import com.xxl.mq.core.openapi.model.MessageData;

/**
 * consumer
 *
 * Created by xuxueli on 16/8/28.
 */
public interface Consumer {

    /**
     * consume message
     *
     * @param message
     */
    void consume(MessageData message);

}