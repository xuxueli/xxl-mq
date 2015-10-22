package com.xxl.mq.destination.impl;

import com.xxl.mq.destination.Destination;

public class Topic implements Destination {
	
	public Topic() {
	}
	public Topic(String topicName) {
		super();
		this.topicName = topicName;
	}

	private String topicName;

	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	
}
