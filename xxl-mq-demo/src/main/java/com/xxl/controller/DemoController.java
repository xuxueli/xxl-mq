package com.xxl.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxl.core.model.QueueMessage;
import com.xxl.core.model.TopicMessage;
import com.xxl.mq.client.MessageProducer;
import com.xxl.mq.util.JacksonUtil;

@Controller
public class DemoController {
	
	@Autowired
	private MessageProducer topic01Producer;
	
	@Autowired
	private MessageProducer queue01Producer;
	
	@RequestMapping("/send/{type}/{msg}")
	@ResponseBody
	public String send(@PathVariable int type, @PathVariable String msg){
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("paramMsg", System.currentTimeMillis());
		
		if (type == 1) {
			TopicMessage message = new TopicMessage();
			message.setInvokeRequest(JacksonUtil.writeValueAsString(params));
			topic01Producer.send(message);
		} else if (type == 2) {
			QueueMessage message = new QueueMessage();
			message.setInvokeRequest(JacksonUtil.writeValueAsString(params));
			queue01Producer.send(message);
		}
		
		return "S:" + System.currentTimeMillis();
	}
	
}
