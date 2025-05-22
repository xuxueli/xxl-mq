package com.xxl.mq.sample.springboot.controller;

import com.xxl.mq.core.XxlMqHelper;
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

		if (type == 1) {

			/**
			 * 1、并行消息：
			 * 		实现方式：消息中心，Topic属性 “分区策略” 选择为 “轮询” 或 “随机”
			 * 		测试Topic：	topic_sample
			 */
			XxlMqHelper.produce("topic_sample", "并行消息");

		} else if (type == 2) {

			/**
			 * 2、串行消息：
			 * 		实现方式：消息中心，Topic属性 “分区策略” 选择为 “第一个” 或 “最后一个”
			 * 		测试Topic：topic_sample_02
			 */
			XxlMqHelper.produce("topic_sample_02", "串行消息");

		} else if (type == 3) {

			/**
			 * 3、分片消息：
			 * 		实现方式：
			 * 			a、消息中心：Topic 路由策略 = “Hash”；
			 * 			b、客户端：生产消息时，自定义消息 “BizId”（作为 “分区hash“ 的 ”业务参数“ ）。相同 BizId 的消息将会被Hash到同一个分区，由同一个消费者串行消费执行；不同 BizId 的消息数据，将会在多个分区并行执行；
			 * 		测试Topic：	topic_sample_03
			 */
			long bizId = 1000;
			XxlMqHelper.produce("topic_sample_03", "分片消息", -1, bizId);

		} else if (type == 4) {

			/**
			 * 4、广播消费：
			 * 		实现方式：消息中心，Topic属性 “分区策略” 选择为 “广播”
			 * 		测试Topic：	topic_sample_04
			 */
			XxlMqHelper.produce("topic_sample_04", "广播消费");


		} else if (type == 5) {

			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, 3);
			Date effectTime = calendar.getTime();

			/**
			 * 5、延时消息（模拟 延时3min）：
			 * 		实现方式：客户端，消息生产时自定义设置 “effectTime”，消息将在指定时间触发消费；
			 * 		测试Topic：	topic_sample_05
			 */
			XxlMqHelper.produce("topic_sample_05", "延时消息（模拟 延时3min）",  effectTime.getTime());


		} else if (type == 6) {

			/**
			 * 6、失败重试消息（模拟 重试3次）：
			 * 		实现方式：消息中心，Topic属性 “重试次数” 设置大于零，并设置 “重试策略”、“重试间隔” 即可。
			 * 		测试Topic：	topic_sample_06
			 */
			XxlMqHelper.produce("topic_sample_06", "失败重试消息（模拟 重试3次））");


		} else if (type == 7) {

			/**
			 * 7、性能测试（模拟 生产10000条消息）：
			 * 		实现方式：并发提交大批量消息，验证消费速率及成功率；
			 * 		测试Topic：	topic_sample_07
			 */
			int msgCount = 10000;
			long start = System.currentTimeMillis();
			for (int i = 0; i < msgCount; i++) {
				XxlMqHelper.produce("topic_sample_07", "性能测试（模拟生产10000条消息）");
			}
			long end = System.currentTimeMillis();
			return "Send message count : "+ msgCount +", Cost : " + (end-start) + " ms";

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
