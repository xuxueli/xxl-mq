package com.xxl.mq.destination.impl;

import com.xxl.mq.destination.Destination;

public class Queue implements Destination {
	
	public Queue() {
	}
	public Queue(String queueName) {
		super();
		this.queueName = queueName;
	}


	private String queueName;

	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	
}
