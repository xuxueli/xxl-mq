package com.xxl.mq.spring;

import java.io.Serializable;

import com.xxl.core.model.QueueMessage.StatusEnum;

public interface MessageListener {
	public StatusEnum onMessage(Serializable message);
}
