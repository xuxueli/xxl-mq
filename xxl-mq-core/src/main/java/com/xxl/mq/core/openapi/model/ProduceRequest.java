package com.xxl.mq.core.openapi.model;

import java.io.Serializable;
import java.util.List;

public class ProduceRequest implements Serializable {
    private static final long serialVersionUID = 42L;

    private List<MessageData> messageList;

    public List<MessageData> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<MessageData> messageList) {
        this.messageList = messageList;
    }

    @Override
    public String toString() {
        return "ProduceRequest{" +
                "messageList=" + messageList +
                '}';
    }
}
