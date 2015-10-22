package com.xxl.dao;

import java.util.List;

import com.xxl.core.model.QueueMessage;

public interface IQueueMessageDao {

	public int insert(QueueMessage message);
	
	public List<QueueMessage> selectList(int pagesize);
	
	public int descRetryCount(int id);
	
	public int updateStatus(int id, int status);
	
}
