package com.xxl.mq.jms.impl;

import com.xxl.mq.util.JacksonUtil;

import javax.jms.Message;
import javax.jms.MessageListener;

public class MessageListenerImpl implements MessageListener {

	@Override
	public void onMessage(Message message) {
		// TODO Auto-generated method stub
		System.out.println("receive msg:" + JacksonUtil.writeValueAsString(message));
	}

}
