package com.xxl.mq.example.controller;

import com.xxl.mq.client.producer.XxlMqProducer;
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
	public String index(){
		return "index";
	}

	@RequestMapping("/produce")
	@ResponseBody
	public String produce(String name){

		// 消息数据
		Map<String, String> data = new HashMap<String, String>();
		data.put("时间戳", System.currentTimeMillis()+"");

		// 生产队列消息
		XxlMqProducer.produce(name, data);

		return "SUCCESS";
	}

	@RequestMapping("/broadcast")
	@ResponseBody
	public String broadcast(String name){

		// 消息数据
		Map<String, String> data = new HashMap<String, String>();
		data.put("时间戳", System.currentTimeMillis()+"");

		// 生产广播消息
		XxlMqProducer.broadcast(name, data);

		return "SUCCESS";
	}

}
