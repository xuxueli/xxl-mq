package com.xxl.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.xxl.core.model.QueueLock;
import com.xxl.core.model.QueueMessage;
import com.xxl.core.model.TopicLog;
import com.xxl.core.model.TopicMessage;
import com.xxl.dao.IQueueConsumerDao;
import com.xxl.dao.IQueueLockDao;
import com.xxl.dao.IQueueMessageDao;
import com.xxl.dao.ITopicLogDao;
import com.xxl.dao.ITopicMessageDao;
import com.xxl.mq.spring.IMessageService;

@Service("messageService")
public class MessageService implements IMessageService {
	private static Logger logger = LoggerFactory.getLogger(MessageService.class);
	
	@Autowired
	private ITopicMessageDao topicMessageDao;
	@Autowired
	private ITopicLogDao topicLogDao;
	@Autowired
	private IQueueMessageDao queueMessageDao;
	@Autowired
	private IQueueLockDao queueLockDao;
	@Autowired
	private IQueueConsumerDao queueConsumerDao;
	
	@Override
	public int addTopidMessage(TopicMessage message) {
		return topicMessageDao.insert(message);
	}
	
	@Override
	public List<TopicMessage> selectListTopic(String topicName, int lifetime, String consumerUuid, int pagesize){
		return topicMessageDao.selectList(topicName, lifetime, consumerUuid, pagesize);
	}
	
	@Override
	public int addTopicLog(int topicMessageId, String consumerUuid) {
		TopicLog log = new TopicLog();
		log.setTopicMessageId(topicMessageId);
		log.setConsumerUuid(consumerUuid);
		log.setLogTime(new Date());
		return topicLogDao.insert(log);
	}
	
	@Override
	public int cleanDeadTopic(int lifetime) {
		int ret = topicLogDao.cleanDeadTopic(lifetime);
		logger.debug("clean dead topic message success, size:{}", ret);
		ret = topicMessageDao.cleanDeadTopic(lifetime);
		logger.debug("clean dead topic log success, size:{}", ret);
		return ret;
	}

	// ------------------------------------------------------------------------
	@Override
	public QueueLock getQueueLock(String queueName) {
		return queueLockDao.getQueueLock(queueName);
	}
	
	@Override
	public int insertQueueLock(String queueName, String consumerUuid) {
		QueueLock lock = new QueueLock();
		lock.setQueueName(queueName);
		lock.setConsumerUuid(consumerUuid);
		lock.setLockTime(new Date());
		return queueLockDao.insert(lock);
	}

	@Override
	public int competeQueueLock(String queueName, String consumerUuid, int lifetime) {
		return queueLockDao.competeQueueLock(queueName, consumerUuid, lifetime);
	}

	@Override
	public int cleanDeadQueueLock(int lifetime) {
		return queueLockDao.cleanDeadQueueLock(lifetime);
	}
	
	@Override
	public int addQueueMessage(QueueMessage message) {
		return queueMessageDao.insert(message);
	}

	@Override
	public List<QueueMessage> selectListQueue(int successStatus, int pagesize) {
		return queueMessageDao.selectList(successStatus, pagesize);
	}

	@Override
	public int descQueueRetryCount(int id, int successStatus) {
		return queueMessageDao.descRetryCount(id, successStatus);
	}

	@Override
	public int updateQueueStatus(int id, int status) {
		return queueMessageDao.updateStatus(id, status);
	}

	@Override
	public int cleanSuccessQueueMessage() {
		return queueMessageDao.cleanSuccessQueueMessage();
	}

	@Override
	public int freshQueueConsumer(String queueName, String consumerUuid) {
		return queueConsumerDao.freshQueueConsumer(queueName, consumerUuid);
	}

	@Override
	public int addQueueConsumer(String queueName, String consumerUuid) {
		return queueConsumerDao.addQueueConsumer(queueName, consumerUuid);
	}

	@Override
	public Map<String, Object> getQueueConsumerRank(String queueName, String consumerUuid) {
		return queueConsumerDao.getQueueConsumerRank(queueName, consumerUuid);
	}

	@Override
	public List<QueueMessage> selectListQueueByConsumer(int successStatus, int pagesize, int rank, int count) {
		return queueMessageDao.selectListQueueByConsumer(successStatus, pagesize, rank, count);
	}

	@Override
	public int cleanDeadQueueConsumer(int lifetime) {
		return queueConsumerDao.cleanDeadQueueConsumer(lifetime);
	}
	
}
