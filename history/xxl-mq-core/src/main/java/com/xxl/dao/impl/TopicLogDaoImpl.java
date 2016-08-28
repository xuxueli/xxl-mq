package com.xxl.dao.impl;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xxl.core.model.TopicLog;
import com.xxl.dao.ITopicLogDao;

@Repository
public class TopicLogDaoImpl implements ITopicLogDao {
	@Autowired
	private SqlSessionTemplate xxlMqSqlSessionTemplate;

	@Override
	public int insert(TopicLog log) {
		return xxlMqSqlSessionTemplate.insert("TopicLogMapper.insert", log);
	}

	@Override
	public int cleanDeadTopic(int lifetime) {
		return xxlMqSqlSessionTemplate.delete("TopicLogMapper.cleanDeadTopic", lifetime);
	}

	
}
