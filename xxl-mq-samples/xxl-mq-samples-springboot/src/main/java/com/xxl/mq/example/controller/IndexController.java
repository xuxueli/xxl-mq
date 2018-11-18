package com.xxl.mq.example.controller;

import com.xxl.mq.client.message.XxlMqMessage;
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
	public String index(){
		return "index";
	}

	@RequestMapping("/produce")
	@ResponseBody
	public String produce(int type){

		String topic = "topic_1";
		String data = "时间戳:" + System.currentTimeMillis();

		if (type == 0) {
			/**
			 * 并行消费
			 */
			XxlMqProducer.produce(topic, data);
		} else if (type == 1) {
			/**
			 * 串行消费
			 */
			XxlMqMessage mqMessage = new XxlMqMessage();
			mqMessage.setTopic(topic);
			mqMessage.setData(data);
			mqMessage.setShardingId(1);

			XxlMqProducer.produce(mqMessage);
		} else if (type == 2) {
			/**
			 * 广播消费
			 */
			XxlMqMessage mqMessage = new XxlMqMessage();
			mqMessage.setTopic(topic);
			mqMessage.setData(data);

			XxlMqProducer.broadcast(mqMessage);
		} else {
			return "Type Error.";
		}

		return "SUCCESS";
	}

}
