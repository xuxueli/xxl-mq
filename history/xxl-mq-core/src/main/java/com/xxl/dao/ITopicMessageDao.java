package com.xxl.dao;

import com.xxl.core.model.TopicMessage;

import java.util.List;

public interface ITopicMessageDao {

	public int insert(TopicMessage message);
	
	public List<TopicMessage> selectList(String topicName, int lifetime, String consumerUuid, int pagesize);

	public int cleanDeadTopic(int lifetime);
	
}
