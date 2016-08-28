package com.xxl.dao.impl;

import com.xxl.core.model.QueueLock;
import com.xxl.dao.IQueueLockDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class QueueLockDaoImpl implements IQueueLockDao {
	@Autowired
	private SqlSessionTemplate xxlMqSqlSessionTemplate;

	@Override
	public QueueLock getQueueLock(String queueName) {
		return xxlMqSqlSessionTemplate.selectOne("QueueLockMapper.getQueueLock", queueName);
	}

	@Override
	public int insert(QueueLock lock) {
		return xxlMqSqlSessionTemplate.insert("QueueLockMapper.insert", lock);
	}

	@Override
	public int competeQueueLock(String queueName, String consumerUuid, int lifetime) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("queueName", queueName);
		params.put("consumerUuid", consumerUuid);
		params.put("lifetime", lifetime);
		return xxlMqSqlSessionTemplate.update("QueueLockMapper.competeQueueLock", params);
	}

	@Override
	public int cleanDeadQueueLock(int lifetime) {
		return xxlMqSqlSessionTemplate.delete("QueueLockMapper.cleanDeadQueueLock", lifetime);
	}
	
}
