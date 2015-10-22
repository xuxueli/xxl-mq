package com.xxl.core.model;


import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class TopicMessage implements Serializable {
	
	private int id;			
	private String topicName;		// topic
	private String invokeRequest;	// topic请求信息 
	private Date effectTime;		// 生效时间，支持delay

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
