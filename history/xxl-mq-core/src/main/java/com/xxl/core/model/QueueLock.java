package com.xxl.core.model;


import java.io.Serializable;
import java.util.Date;

public class QueueLock implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String queueName;
	private String consumerUuid;
	private Date lockTime;
	
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
	public String getConsumerUuid() {
		return consumerUuid;
	}
	public void setConsumerUuid(String consumerUuid) {
		this.consumerUuid = consumerUuid;
	}
	public Date getLockTime() {
		return lockTime;
	}
	public void setLockTime(Date lockTime) {
		this.lockTime = lockTime;
	}
	
}
