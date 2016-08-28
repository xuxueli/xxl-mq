package com.xxl.mq.broker.controller;

import com.xxl.mq.client.XxlMqProducer;
import com.xxl.mq.client.message.Message;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
public class IndexController {

	@RequestMapping("/")
	@ResponseBody
	public String loginDo(HttpServletRequest request, HttpServletResponse response, String userName, String password, String ifRemember){

		Message message = new Message("test", new HashMap<String, String>());
		XxlMqProducer.saveMessage(message);

		return "SUCCESS";
	}

}
