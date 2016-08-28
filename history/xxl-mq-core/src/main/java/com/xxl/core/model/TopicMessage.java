package com.xxl.core.model;


import java.io.Serializable;
import java.util.Date;

public class TopicMessage implements Serializable {
	private static final long serialVersionUID = -1790698904854014512L;
	
	private int id;			
	private String topicName;				// Must
	private String invokeRequest;
	private Date effectTime = new Date();	// Must

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	public Date getEffectTime() {
		return effectTime;
	}
	public void setEffectTime(Date effectTime) {
		this.effectTime = effectTime;
	}
	public String getInvokeRequest() {
		return invokeRequest;
	}
	public void setInvokeRequest(String invokeRequest) {
		this.invokeRequest = invokeRequest;
	}
	
}
