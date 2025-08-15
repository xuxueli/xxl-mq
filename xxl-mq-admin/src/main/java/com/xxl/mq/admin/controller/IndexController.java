package com.xxl.mq.admin.controller;

import com.xxl.mq.admin.service.MessageService;
import com.xxl.sso.core.annotation.XxlSso;
import com.xxl.tool.response.Response;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
public class IndexController {

	@Resource
	private MessageService messageService;

	@RequestMapping("/")
	@XxlSso
	public String defaultpage(Model model) {
		return "redirect:/index";
	}

	@RequestMapping("/index")
	@XxlSso
	public String index(HttpServletRequest request, Model model) {

		// dashboardInfo
		Map<String, Object> dashboardInfo = messageService.dashboardInfo();
		model.addAttribute("dashboardInfo", dashboardInfo);

		return "index";
	}

	@RequestMapping("/chartInfo")
	@ResponseBody
	@XxlSso
	public Response<Map<String, Object>> chartInfo(@RequestParam("startDate") Date startDate, @RequestParam("endDate") Date endDate) {
		return messageService.chartInfo(startDate, endDate);
	}

	@RequestMapping("/help")
	@XxlSso
	public String help() {
		return "help";
	}


	@RequestMapping(value = "/errorpage")
	@XxlSso(login = false)
	public ModelAndView errorPage(HttpServletRequest request, HttpServletResponse response, ModelAndView mv) {

		String exceptionMsg = "HTTP Status Code: "+response.getStatus();

		mv.addObject("exceptionMsg", exceptionMsg);
		mv.setViewName("common/common.errorpage");
		return mv;
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormat.setLenient(false);
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
	}
	
}
