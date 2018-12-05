package com.xxl.mq.sample.springboot.controller;

import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.producer.XxlMqProducer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Calendar;
import java.util.Date;

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
			XxlMqProducer.produce(new XxlMqMessage(topic, data));

		} else if (type == 1) {

			/**
			 * 串行消费
			 */
			XxlMqProducer.produce(new XxlMqMessage(topic, data, 1L));

		} else if (type == 2) {

			/**
			 * 广播消费
			 */
			XxlMqProducer.broadcast(new XxlMqMessage(topic, data));


		} else if (type == 3) {

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, 5);
			Date effectTime = calendar.getTime();

			/**
			 * 延时消息
			 */
			XxlMqProducer.produce(new XxlMqMessage(topic, data, effectTime));

		} else if (type == 4) {

			int msgNum = 10000;
			long start = System.currentTimeMillis();
			for (int i = 0; i < msgNum; i++) {
				XxlMqProducer.produce(new XxlMqMessage("topic_1", "No:"+i));
			}
			long end = System.currentTimeMillis();
			return "Cost = " + (end-start);

		} else {
			return "Type Error.";
		}

		return "SUCCESS";
	}

	@ExceptionHandler({Exception.class})
     public String exception(Exception e) {
         e.printStackTrace();
         return e.getMessage();
     }

}
