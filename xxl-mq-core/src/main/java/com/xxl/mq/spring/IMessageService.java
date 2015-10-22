package com.xxl.mq.spring;

import java.util.List;

import com.xxl.core.model.QueueMessage;
import com.xxl.core.model.TopicMessage;
import com.xxl.mq.destination.impl.Queue;
import com.xxl.mq.destination.impl.Topic;
import com.xxl.mq.message.impl.ObjectMessage;

public interface IMessageService {

	public int addTopidMessage(Topic topic, ObjectMessage message);

	public int addQueueMessage(Queue queue, ObjectMessage message);
	
	public List<TopicMessage> selectListTopic(int pagesize);
	
	public List<QueueMessage> selectListQueue(int pagesize);
}
