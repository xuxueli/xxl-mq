package com.xxl.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xxl.core.model.QueueMessage;
import com.xxl.core.model.QueueMessage.StatusEnum;
import com.xxl.core.model.TopicMessage;
import com.xxl.dao.IQueueMessageDao;
import com.xxl.dao.ITopicMessageDao;
import com.xxl.mq.destination.impl.Queue;
import com.xxl.mq.destination.impl.Topic;
import com.xxl.mq.message.impl.ObjectMessage;
import com.xxl.mq.spring.IMessageService;

@Service
public class MessageService implements IMessageService {
	
	@Autowired
	private ITopicMessageDao topicMessageDao;
	@Autowired
	private IQueueMessageDao queueMessageDao;
	
	@Override
	public int addTopidMessage(Topic topic, ObjectMessage message) {
		
		// validate
		if (topic.getTopicName() == null || topic.getTopicName().trim().length() == 0) {
			return 0;
		}
		if (message.getJsonParam() == null || message.getJsonParam().trim().length() == 0) {
			return 0;
		}
		if (message.getEffectTime() == null){
			message.setEffectTime(new Date());
		}
		
		// save
		TopicMessage topicMessage = new TopicMessage();
		topicMessage.setTopicName(topic.getTopicName());
		topicMessage.setInvokeRequest(message.getJsonParam());
		topicMessage.setEffectTime(message.getEffectTime());
		
		return topicMessageDao.insert(topicMessage);
	}

	@Override
	public int addQueueMessage(Queue queue, ObjectMessage message) {
		
		// validate
		if (queue.getQueueName() == null || queue.getQueueName().trim().length() == 0) {
			return 0;
		}
		if (message.getJsonParam() == null || message.getJsonParam().trim().length() == 0) {
			return 0;
		}
		if (message.getEffectTime() == null){
			message.setEffectTime(new Date());
		}
		
		// save
		QueueMessage queueMessage = new QueueMessage();
		queueMessage.setQueueName(queue.getQueueName());
		queueMessage.setInvokeRequest(message.getJsonParam());
		queueMessage.setEffectTime(message.getEffectTime());
		queueMessage.setStatus(StatusEnum.NEW.getCode());
		queueMessage.setRetryCount(2);
		
		return queueMessageDao.insert(queueMessage);
	}
	
	@Override
	public List<TopicMessage> selectListTopic(int pagesize){
		return topicMessageDao.selectList(pagesize);
	}

	@Override
	public List<QueueMessage> selectListQueue(int pagesize) {
		return queueMessageDao.selectList(pagesize);
	}
	
}
