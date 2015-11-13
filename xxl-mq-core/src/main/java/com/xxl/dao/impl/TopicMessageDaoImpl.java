package com.xxl.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xxl.core.model.TopicMessage;
import com.xxl.dao.ITopicMessageDao;

@Repository
public class TopicMessageDaoImpl implements ITopicMessageDao {
	@Autowired
	private SqlSessionTemplate xxlMqSqlSessionTemplate;

	@Override
	public int insert(TopicMessage message) {
		return xxlMqSqlSessionTemplate.insert("TopicMessageMapper.insert", message);
	}

	@Override
	public List<TopicMessage> selectList(String topicName, int lifetime, String consumerUuid, int pagesize) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("topicName", topicName);
		params.put("lifetime", lifetime);
		params.put("consumerUuid", consumerUuid);
		params.put("pagesize", pagesize);
		return xxlMqSqlSessionTemplate.selectList("TopicMessageMapper.selectList", params);
	}

	@Override
	public int cleanDeadTopic(int lifetime) {
		return xxlMqSqlSessionTemplate.delete("TopicMessageMapper.cleanDeadTopic", lifetime);
	}
	
}
