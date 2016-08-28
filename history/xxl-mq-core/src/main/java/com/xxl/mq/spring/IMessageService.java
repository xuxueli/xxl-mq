package com.xxl.mq.spring;

import com.xxl.core.model.QueueLock;
import com.xxl.core.model.QueueMessage;
import com.xxl.core.model.TopicMessage;

import java.util.List;
import java.util.Map;

public interface IMessageService {

	public int addTopidMessage(TopicMessage message);
	
	/**
	 * load effective topic
	 * @param lifetime		生存周期 /秒
	 * @param consumerUuid	consumer uuid
	 * @param pagesize		单次查询记录数
	 * @return
	 */
	public List<TopicMessage> selectListTopic(String topicName, int lifetime, String consumerUuid, int pagesize);
	
	public int addTopicLog(int topicMessageId, String consumerUuid);
	public int cleanDeadTopic(int lifetime);
	
	// ------------------------------------------------------------------------
	public QueueLock getQueueLock(String queueName);
	public int insertQueueLock(String queueName, String consumerUuid);
	public int competeQueueLock(String queueName, String consumerUuid, int lifetime);
	public int cleanDeadQueueLock(int lifetime);

	public int addQueueMessage(QueueMessage message);
	
	/**
	 * load effective topic, status != successStatus AND retry_count > 0 AND effect_time < NOW()
	 * @param successStatus
	 * @param pagesize
	 * @return
	 */
	public List<QueueMessage> selectListQueue(int successStatus, int pagesize);
	
	/**
	 * retry_count -1, status = success
	 * @param id
	 * @param successStatus
	 * @return
	 */
	public int descQueueRetryCount(int id, int successStatus);
	public int updateQueueStatus(int id, int status);

	public int cleanSuccessQueueMessage();

	public int freshQueueConsumer(String queueName, String consumerUuid);
	public int addQueueConsumer(String queueName, String consumerUuid);
	public Map<String, Object> getQueueConsumerRank(String queueName, String consumerUuid);

	public List<QueueMessage> selectListQueueByConsumer(int successStatus, int pagesize, int rank, int count);

	public int cleanDeadQueueConsumer(int lifetime);
	
}
