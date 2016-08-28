package com.xxl.core.model;


import java.io.Serializable;
import java.util.Date;

public class TopicLog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int topicMessageId;
	private String consumerUuid;
	private Date logTime;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getTopicMessageId() {
		return topicMessageId;
	}
	public void setTopicMessageId(int topicMessageId) {
		this.topicMessageId = topicMessageId;
	}
	public String getConsumerUuid() {
		return consumerUuid;
	}
	public void setConsumerUuid(String consumerUuid) {
		this.consumerUuid = consumerUuid;
	}
	public Date getLogTime() {
		return logTime;
	}
	public void setLogTime(Date logTime) {
		this.logTime = logTime;
	}
	
}
