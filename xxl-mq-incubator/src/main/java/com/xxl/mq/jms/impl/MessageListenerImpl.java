package com.xxl.mq.jms.impl;

import javax.jms.Message;
import javax.jms.MessageListener;

import com.xxl.mq.util.JacksonUtil;

public class MessageListenerImpl implements MessageListener {

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		System.out.println("receive msg:" + JacksonUtil.writeValueAsString(message));
	}

}
