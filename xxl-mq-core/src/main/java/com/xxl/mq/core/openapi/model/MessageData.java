package com.xxl.mq.core.openapi.model;

import java.io.Serializable;

/**
 * message data
 *
 * Created by xuxueli on 16/8/28.
 */
public class MessageData implements Serializable {
    private static final long serialVersionUID = 42L;

    // todo
    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
