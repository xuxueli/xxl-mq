package com.xxl.core.model;


import java.io.Serializable;
import java.util.Date;

public class QueueMessage implements Serializable {
	private static final long serialVersionUID = 5899808874393055955L;

	private int id;
	private String queueName;							// Must
	private String invokeRequest;
	private Date effectTime = new Date();				// Must
	private int status = StatusEnum.NEW.getStatus();
	private int retryCount = 1;
	private int retryCountLog;
	
	/**
	 * 执行状态字典
	 */
	public enum StatusEnum{
		NEW(0), SUCCESS(1), FAIL(2);
		private int status;
		private StatusEnum(int status) {
			this.status = status;
		}
		public int getStatus() {
			return status;
		}
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getQueueName() {
		return queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public String getInvokeRequest() {
		return invokeRequest;
	}
	public void setInvokeRequest(String invokeRequest) {
		this.invokeRequest = invokeRequest;
	}
	public Date getEffectTime() {
		return effectTime;
	}
	public void setEffectTime(Date effectTime) {
		this.effectTime = effectTime;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	public int getRetryCountLog() {
		return retryCountLog;
	}
	public void setRetryCountLog(int retryCountLog) {
		this.retryCountLog = retryCountLog;
	}
	
}
