package com.xxl.mq.spring;

import java.io.Serializable;

public interface MessageListener {
	public void onMessage(Serializable message);
}
