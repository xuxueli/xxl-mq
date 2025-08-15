package com.xxl.mq.admin.web.interceptor;

import com.xxl.mq.admin.constant.enums.RoleEnum;
import com.xxl.mq.admin.model.dto.ResourceDTO;
import com.xxl.mq.admin.util.I18nUtil;
import com.xxl.sso.core.helper.XxlSsoHelper;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.freemarker.FtlTool;
import com.xxl.tool.response.Response;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xuxueli 2015-12-12 18:09:04
 */
@Component
public class CommonDataInterceptor implements AsyncHandlerInterceptor {

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

		if (modelAndView != null) {
			// i18n, static method
			modelAndView.addObject("I18nUtil", FtlTool.generateStaticModel(I18nUtil.class.getName()));

			// fill menu data
			fillMenuData(request, modelAndView);
		}
	}

	/**
	 * fill menu data
	 *
	 * @param request
	 * @param modelAndView
	 */
	private void fillMenuData(HttpServletRequest request, ModelAndView modelAndView){

		// login check
		Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
		if (!loginInfoResponse.isSuccess()) {
			return;
		}

		// init menu-list
		List<ResourceDTO> resourceDTOList = Arrays.asList(
				new ResourceDTO(1, 0, "首页",1, "", "/index", "fa fa-home", 1, 0, null),
				new ResourceDTO(2, 0, "主题管理",1, "", "/topic", " fa-cubes", 2, 0, null),
				new ResourceDTO(3, 0, "消息管理",1, "", "/message", " fa-database", 3, 0, null),
				new ResourceDTO(4, 0, "归档消息",1, "", "/messageArchive", " fa-database", 4, 0, null),
				new ResourceDTO(5, 0, "服务管理",1, "ADMIN", "/application", " fa-cloud", 5, 0,null),
				new ResourceDTO(6, 0, "系统管理",0, "ADMIN", "/system", "fa-cog", 6, 0, Arrays.asList(
						new ResourceDTO(7, 5, "AccessToken",1, "ADMIN", "/accesstoken", "fa-key", 7, 0, null),
						new ResourceDTO(8, 5, "用户管理",1, "ADMIN", "/user", "fa-users", 8, 0, null)
				)),
				new ResourceDTO(9, 0, "帮助中心",1, "", "/help", "fa-book", 9, 0, null)
		);
		// filter by role
		if (!XxlSsoHelper.hasRole(loginInfoResponse.getData(), RoleEnum.ADMIN.getValue()).isSuccess()) {
			resourceDTOList = resourceDTOList.stream()
					.filter(resourceDTO -> StringTool.isBlank(resourceDTO.getPermission() ))	// normal user had no permission
					.collect(Collectors.toList());
		}
		resourceDTOList.stream().sorted(Comparator.comparing(ResourceDTO::getOrder)).toList();

		modelAndView.addObject("resourceList", resourceDTOList);
	}


}
