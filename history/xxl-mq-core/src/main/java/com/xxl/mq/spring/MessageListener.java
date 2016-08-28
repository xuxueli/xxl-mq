package com.xxl.mq.spring;

import com.xxl.core.model.QueueMessage.StatusEnum;

import java.io.Serializable;

public interface MessageListener {
	public StatusEnum onMessage(Serializable message);
}
