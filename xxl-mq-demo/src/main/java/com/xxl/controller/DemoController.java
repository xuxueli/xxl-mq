package com.xxl.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxl.mq.client.MessageProducer;
import com.xxl.mq.message.impl.ObjectMessage;
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
		params.put("paramMsg", msg);
		ObjectMessage objectMessage = new ObjectMessage();
		objectMessage.setJsonParam(JacksonUtil.writeValueAsString(params));
		objectMessage.setEffectTime(new Date());
		
		if (type == 1) {
			topic01Producer.send(objectMessage);
		} else if (type == 2) {
			queue01Producer.send(objectMessage);
		}
		
		return "S";
	}
	
}
