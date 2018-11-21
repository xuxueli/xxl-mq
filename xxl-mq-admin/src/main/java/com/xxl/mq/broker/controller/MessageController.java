package com.xxl.mq.broker.controller;

import com.xxl.mq.broker.controller.annotation.PermessionLimit;
import com.xxl.mq.broker.core.result.ReturnT;
import com.xxl.mq.broker.service.IXxlMqMessageService;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.util.DateFormatUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Base 
 * @author xuxueli 2016-3-19 13:56:28
 */
@Controller
@RequestMapping("/message")
public class MessageController {

	@Resource
	private IXxlMqMessageService xxlMqMessageService;

	@RequestMapping("")
	@PermessionLimit
	public String index(Model model, String topic){

		model.addAttribute("status", XxlMqMessageStatus.values());
		model.addAttribute("topic", topic);

		return "message/message.index";
	}
	
	@RequestMapping("/pageList")
	@ResponseBody
	@PermessionLimit
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
										@RequestParam(required = false, defaultValue = "10") int length,
										String topic,
										String status,
										String filterTime){

		// parse param
		Date addTimeStart = null;
		Date addTimeEnd = null;
		if (filterTime!=null && filterTime.trim().length()>0) {
			String[] temp = filterTime.split(" - ");
			if (temp!=null && temp.length == 2) {
				try {
					addTimeStart = DateFormatUtil.parseDateTime(temp[0]);
					addTimeEnd = DateFormatUtil.parseDateTime(temp[1]);
				} catch (ParseException e) {	}
			}
		}


		return xxlMqMessageService.pageList(start, length, topic, status, addTimeStart, addTimeEnd);
	}
	
	@RequestMapping("/delete")
	@ResponseBody
	@PermessionLimit
	public ReturnT<String> delete(int id){
		return xxlMqMessageService.delete(id);
	}

	@RequestMapping("/update")
	@ResponseBody
	@PermessionLimit
	public ReturnT<String> update(XxlMqMessage message){
		return xxlMqMessageService.update(message);
	}

	@RequestMapping("/add")
	@ResponseBody
	@PermessionLimit
	public ReturnT<String> add(XxlMqMessage message){
		return xxlMqMessageService.add(message);
	}

}
