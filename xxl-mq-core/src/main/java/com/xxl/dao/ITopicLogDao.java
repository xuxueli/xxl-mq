package com.xxl.dao;

import com.xxl.core.model.TopicLog;

public interface ITopicLogDao {

	public int insert(TopicLog log);

	public int cleanDeadTopic(int lifetime);
	
}
