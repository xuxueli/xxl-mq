package com.xxl.dao.impl;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.xxl.core.model.TopicMessage;
import com.xxl.dao.ITopicMessageDao;

@Repository
public class TopicMessageDaoImpl implements ITopicMessageDao {
	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;

	@Override
	public int insert(TopicMessage message) {
		return sqlSessionTemplate.insert("TopicMessageMapper.insert", message);
	}

	@Override
	public List<TopicMessage> selectList(int pagesize) {
		return sqlSessionTemplate.selectList("TopicMessageMapper.selectList", pagesize);
	}
	
}
