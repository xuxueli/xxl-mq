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
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public int insert(QueueMessage message) {
		return sqlSessionTemplate.insert("QueueMessageMapper.insert", message);
	}

	@Override
	public List<QueueMessage> selectList(int pagesize) {
		return sqlSessionTemplate.selectList("QueueMessageMapper.selectList", pagesize);
	}

	@Override
	public int descRetryCount(int id) {
		return sqlSessionTemplate.update("QueueMessageMapper.descRetryCount", id);
	}

	@Override
	public int updateStatus(int id, int status) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		params.put("status", status);
		return sqlSessionTemplate.update("QueueMessageMapper.updateStatus", params);
	}
	
}
