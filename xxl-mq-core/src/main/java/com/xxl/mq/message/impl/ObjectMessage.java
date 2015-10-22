package com.xxl.mq.message.impl;

import java.io.Serializable;
import java.util.Date;

import com.xxl.mq.message.Message;


@SuppressWarnings("serial")
public class ObjectMessage implements Message, Serializable{
	private Date effectTime;
	private String jsonParam;
	
	public Date getEffectTime() {
		return effectTime;
	}
	public void setEffectTime(Date effectTime) {
		this.effectTime = effectTime;
	}
	public String getJsonParam() {
		return jsonParam;
	}
	public void setJsonParam(String jsonParam) {
		this.jsonParam = jsonParam;
	}
	
}
