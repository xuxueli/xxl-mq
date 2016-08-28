package com.xxl.dao;

import java.util.Map;

public interface IQueueConsumerDao {

	public int freshQueueConsumer(String queueName, String consumerUuid);

	public int addQueueConsumer(String queueName, String consumerUuid);

	public Map<String, Object> getQueueConsumerRank(String queueName, String consumerUuid);

	public int cleanDeadQueueConsumer(int lifetime);

}
