package com.xxl.core.model;


import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class QueueMessage implements Serializable {
	
	private int id;
	private String queueName;		// queue
	private String invokeRequest;	// 请求信息
	private Date effectTime;		// 生效时间，支持delay
	private int status;				// 执行结果：0-new、1-成功、2-失败
	private int retryCount;			// 失败重试次数
	
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
	
	/**
	 * 执行状态字典
	 */
	public enum StatusEnum{
		NEW(0), SUCCESS(1), FAIL(2);
		private int code;
		private StatusEnum(int code) {
			this.code = code;
		}
		public int getCode() {
			return code;
		}
	}
	
}
