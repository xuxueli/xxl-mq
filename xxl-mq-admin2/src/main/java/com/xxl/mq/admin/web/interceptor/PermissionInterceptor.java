package com.xxl.mq.admin.web.interceptor;

import com.xxl.mq.admin.annotation.Permission;
import com.xxl.mq.admin.constant.enums.RoleEnum;
import com.xxl.mq.admin.model.dto.LoginUserDTO;
import com.xxl.mq.admin.model.dto.ResourceDTO;
import com.xxl.mq.admin.util.I18nUtil;
import com.xxl.mq.admin.service.impl.LoginService;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.exception.BizException;
import com.xxl.tool.freemarker.FtlTool;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限拦截
 *
 * @author xuxueli 2015-12-12 18:09:04
 */
@Component
public class PermissionInterceptor implements AsyncHandlerInterceptor {

	@Resource
	private LoginService loginService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// handler method
		if (!(handler instanceof HandlerMethod)) {
			return true;	// proceed with the next interceptor
		}
		HandlerMethod method = (HandlerMethod)handler;

		// parse permission config
		Permission permission = method.getMethodAnnotation(Permission.class);
		if (permission == null) {
			throw new BizException("权限拦截，请求路径权限未设置");
		}
		if (!permission.login()) {
			return true;	// not need login ,not valid permission, pass
		}

		// valid login
		LoginUserDTO loginUser = loginService.checkLogin(request, response);
		if (loginUser == null) {
			response.setStatus(302);
			response.setHeader("location", request.getContextPath() + "/toLogin");
			return false;
		}
		LoginService.setLoginUser(request, loginUser);

		// valid permission
		if (StringTool.isNotBlank(permission.value())) {
			// need permisson
			RoleEnum roleEnum = RoleEnum.matchByValue(loginUser.getRole());
			if (roleEnum != null && roleEnum.getPermissions().contains(permission.value())) {
				return true;
			} else {
				throw new BizException(I18nUtil.getString("system_permission_limit"));
			}
		}

		return true;
	}

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
		// fill menu-list
		List<ResourceDTO> resourceDTOList = Arrays.asList(
				new ResourceDTO(1, 0, "首页",1, "", "/index", "fa fa-home", 1, 0, null),
				new ResourceDTO(2, 0, "主题管理",1, "", "/topic", " fa-cubes", 2, 0, null),
				new ResourceDTO(3, 0, "消息管理",1, "", "/message", " fa-database", 3, 0, null),
				new ResourceDTO(4, 0, "服务管理",1, "ADMIN", "/application", " fa-cloud", 4, 0,null),
				new ResourceDTO(5, 0, "系统管理",0, "ADMIN", "/system", "fa-cog", 5, 0, Arrays.asList(
						new ResourceDTO(6, 5, "AccessToken",1, "ADMIN", "/accesstoken", "fa-key", 6, 0, null),
						new ResourceDTO(7, 5, "用户管理",1, "ADMIN", "/user", "fa-users", 7, 0, null)
				)),
				new ResourceDTO(8, 0, "帮助中心",1, "", "/help", "fa-book", 8, 0, null)
		);
		// valid
		if (!loginService.isAdmin(request)) {
			resourceDTOList = resourceDTOList.stream()
					.filter(resourceDTO -> StringTool.isBlank(resourceDTO.getPermission() ))	// normal user had no permission
					.collect(Collectors.toList());
		}
		resourceDTOList.stream().sorted(Comparator.comparing(ResourceDTO::getOrder)).collect(Collectors.toList());

		modelAndView.addObject("resourceList", resourceDTOList);
	}


}
