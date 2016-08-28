package com.xxl.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xxl.core.model.QueueMessage;
import com.xxl.dao.IQueueMessageDao;

@Repository
public class QueueMessageDaoImpl implements IQueueMessageDao {
	@Autowired
	private SqlSessionTemplate xxlMqSqlSessionTemplate;

	@Override
	public int insert(QueueMessage message) {
		return xxlMqSqlSessionTemplate.insert("QueueMessageMapper.insert", message);
	}

	@Override
	public List<QueueMessage> selectList(int successStatus, int pagesize) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("successStatus", successStatus);
		params.put("pagesize", pagesize);
		return xxlMqSqlSessionTemplate.selectList("QueueMessageMapper.selectList", params);
	}

	@Override
	public int descRetryCount(int id, int successStatus) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		params.put("successStatus", successStatus);
		return xxlMqSqlSessionTemplate.update("QueueMessageMapper.descRetryCount", params);
	}

	@Override
	public int updateStatus(int id, int status) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		params.put("status", status);
		return xxlMqSqlSessionTemplate.update("QueueMessageMapper.updateStatus", params);
	}

	@Override
	public int cleanSuccessQueueMessage() {
		return xxlMqSqlSessionTemplate.delete("QueueMessageMapper.cleanSuccessQueueMessage");
	}

	@Override
	public List<QueueMessage> selectListQueueByConsumer(int successStatus, int pagesize, int rank, int count) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("successStatus", successStatus);
		params.put("pagesize", pagesize);
		params.put("rank", rank);
		params.put("count", count);
		return xxlMqSqlSessionTemplate.selectList("QueueMessageMapper.selectListQueueByConsumer", params);
	}
	
}
