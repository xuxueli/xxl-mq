package com.xxl.mq.jms.impl;

import javax.jms.JMSException;
import javax.jms.Topic;

public class TopicImpl implements Topic {
	
	private String topicName;
	public TopicImpl(String topicName) {
		this.topicName = topicName;
	}

	@Override
	public String getTopicName() throws JMSException {
		return this.topicName;
	}

}
