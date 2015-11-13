package com.xxl.dao;

import java.util.List;

import com.xxl.core.model.TopicMessage;

public interface ITopicMessageDao {

	public int insert(TopicMessage message);
	
	public List<TopicMessage> selectList(String topicName, int lifetime, String consumerUuid, int pagesize);

	public int cleanDeadTopic(int lifetime);
	
}
