package com.xxl.dao;

import com.xxl.core.model.QueueMessage;

import java.util.List;

public interface IQueueMessageDao {

	public int insert(QueueMessage message);
	
	public List<QueueMessage> selectList(int successStatus, int pagesize);
	
	public int descRetryCount(int id, int successStatus);
	
	public int updateStatus(int id, int status);

	public int cleanSuccessQueueMessage();

	public List<QueueMessage> selectListQueueByConsumer(int successStatus, int pagesize, int rank, int count);
	
}
