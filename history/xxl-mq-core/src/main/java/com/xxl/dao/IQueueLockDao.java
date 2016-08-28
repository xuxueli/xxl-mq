package com.xxl.dao;

import com.xxl.core.model.QueueLock;


public interface IQueueLockDao {

	public QueueLock getQueueLock(String queueName);
	public int insert(QueueLock lock);
	public int competeQueueLock(String queueName, String consumerUuid, int lifetime);
	public int cleanDeadQueueLock(int lifetime);

}
