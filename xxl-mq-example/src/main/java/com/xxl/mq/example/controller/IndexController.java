package com.xxl.mq.example.controller;

import com.xxl.mq.client.XxlMqProducer;
import com.xxl.mq.client.message.Message;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
public class IndexController {

	@RequestMapping("/")
	@ResponseBody
	public String loginDo(){

		Map<String, String> map = new HashMap<String, String>();
		map.put("num", System.currentTimeMillis()+"");

		Message message = new Message("test", Message.Destination.TOPIC, map, null);
		XxlMqProducer.saveMessage(message);

		return "SUCCESS";
	}

}
