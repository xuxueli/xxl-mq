package com.xxl.mq.factory;

import com.xxl.mq.spring.IMessageService;


public class ConnectionFactory {
	
	private IMessageService messageService;

	public IMessageService getMessageService() {
		return messageService;
	}
	public void setMessageService(IMessageService messageService) {
		this.messageService = messageService;
	}
	
}
