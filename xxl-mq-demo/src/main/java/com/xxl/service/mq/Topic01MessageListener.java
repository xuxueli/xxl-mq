package com.xxl.service.mq;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.xxl.core.model.QueueMessage.StatusEnum;
import com.xxl.mq.spring.MessageListener;
import com.xxl.mq.util.JacksonUtil;

@Component("topic01MessageListener")
public class Topic01MessageListener implements MessageListener {
	private static Logger logger = LoggerFactory.getLogger(Topic01MessageListener.class);
	
	@Override
	public StatusEnum onMessage(Serializable message) {
		logger.info("######### onMessage :{}", JacksonUtil.writeValueAsString(message));
		return StatusEnum.SUCCESS;
	}

}
