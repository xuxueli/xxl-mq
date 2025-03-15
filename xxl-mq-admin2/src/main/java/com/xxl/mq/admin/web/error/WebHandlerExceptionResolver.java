package com.xxl.mq.admin.web.error;

import com.alibaba.fastjson2.JSON;
import com.xxl.tool.exception.BizException;
import com.xxl.tool.response.Response;
import com.xxl.tool.response.ResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * common exception resolver
 *
 * @author xuxueli 2016-1-6 19:22:18
 */
@Component
public class WebHandlerExceptionResolver implements HandlerExceptionResolver {
	private static transient Logger logger = LoggerFactory.getLogger(WebHandlerExceptionResolver.class);

	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {

		if (!(ex instanceof BizException)) {
			logger.error("WebExceptionResolver:{}", ex);
		}

		// if json
		boolean isJson = false;
		if (handler instanceof HandlerMethod) {
			HandlerMethod method = (HandlerMethod)handler;
			ResponseBody responseBody = method.getMethodAnnotation(ResponseBody.class);
			if (responseBody != null) {
				isJson = true;
			}
		}

		// error result
		Response<String> errorResult = new ResponseBuilder<String>().fail(ex.toString().replaceAll("\n", "<br/>")).build();

		// response
		ModelAndView mv = new ModelAndView();
		if (isJson) {
			try {
				response.setContentType("application/json;charset=utf-8");
				response.getWriter().print(JSON.toJSONString(errorResult));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			return mv;
		} else {

			mv.addObject("exceptionMsg", errorResult.getMsg());
			mv.setViewName("common/common.errorpage");
			return mv;
		}
	}
	
}