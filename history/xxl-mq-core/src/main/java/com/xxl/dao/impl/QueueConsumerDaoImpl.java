package com.xxl.dao.impl;

import com.xxl.dao.IQueueConsumerDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class QueueConsumerDaoImpl implements IQueueConsumerDao {
	@Autowired
	private SqlSessionTemplate xxlMqSqlSessionTemplate;

	@Override
	public int freshQueueConsumer(String queueName, String consumerUuid) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("queueName", queueName);
		params.put("consumerUuid", consumerUuid);
		return xxlMqSqlSessionTemplate.update("QueueConsumerMapper.freshQueueConsumer", params);
	}

	@Override
	public int addQueueConsumer(String queueName, String consumerUuid) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("queueName", queueName);
		params.put("consumerUuid", consumerUuid);
		return xxlMqSqlSessionTemplate.update("QueueConsumerMapper.addQueueConsumer", params);
	}

	@Override
	public Map<String, Object> getQueueConsumerRank(String queueName, String consumerUuid) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("queueName", queueName);
		params.put("consumerUuid", consumerUuid);
		return xxlMqSqlSessionTemplate.selectOne("QueueConsumerMapper.getQueueConsumerRank", params);
	}

	@Override
	public int cleanDeadQueueConsumer(int lifetime) {
		return xxlMqSqlSessionTemplate.delete("QueueConsumerMapper.cleanDeadQueueConsumer", lifetime);
	}
	
}
