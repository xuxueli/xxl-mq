package com.xxl.mq.admin.controller.biz;

import com.xxl.mq.admin.constant.enums.ArchiveStrategyEnum;
import com.xxl.mq.admin.constant.enums.RoleEnum;
import com.xxl.mq.core.constant.MessageStatusEnum;
import com.xxl.mq.admin.model.dto.MessageArchiveDTO;
import com.xxl.mq.admin.model.entity.Application;
import com.xxl.mq.admin.service.ApplicationService;
import com.xxl.mq.admin.service.MessageAichiveService;
import com.xxl.sso.core.annotation.XxlSso;
import com.xxl.sso.core.helper.XxlSsoHelper;
import com.xxl.sso.core.model.LoginInfo;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
* Message Controller
*
* Created by xuxueli on '2025-03-21 21:54:06'.
*/
@Controller
@RequestMapping("/messageArchive")
public class MessageArchiveController {

    @Resource
    private MessageAichiveService messageAichiveService;
    @Resource
    private ApplicationService applicationService;

    /**
    * 页面
    */
    @RequestMapping
    @XxlSso
    public String index(Model model, HttpServletRequest request, String topic) {

        // Enum
        model.addAttribute("MessageStatusEnum", MessageStatusEnum.values());
        model.addAttribute("ArchiveStrategyEnum", ArchiveStrategyEnum.values());

        // appname
        List<Application> applicationList = findPermissionApplication(request, applicationService);
        model.addAttribute("applicationList", applicationList);

        // param
        model.addAttribute("topic", topic);

        return "biz/messageArchive";
    }

    /**
     * find permission application list
     * @param request
     * @return
     */
    public static List<Application> findPermissionApplication(HttpServletRequest request, ApplicationService applicationService){
        List<Application> applicationList = applicationService.findAll().getData();

        // check role - admin
        Response<LoginInfo> loginInfoResponse = XxlSsoHelper.loginCheckWithAttr(request);
        if (!XxlSsoHelper.hasRole(loginInfoResponse.getData(), RoleEnum.ADMIN.getValue()).isSuccess()) {
            // parse appname from login-info
            List<String> appnameList = MapTool.isNotEmpty(loginInfoResponse.getData().getExtraInfo())
                    ? Arrays.asList(loginInfoResponse.getData().getExtraInfo().get("appnameList").split(",")):new ArrayList<>();
            applicationList = applicationList
                    .stream()
                    .filter(application -> appnameList.contains(application.getAppname()))
                    .collect(Collectors.toList());
        }
        return applicationList;
    }

    /**
    * 分页查询
    */
    @RequestMapping("/pageList")
    @ResponseBody
    @XxlSso
    public Response<PageModel<MessageArchiveDTO>> pageList(@RequestParam(required = false, defaultValue = "0") int offset,
                                                           @RequestParam(required = false, defaultValue = "10") int pagesize,
                                                           @RequestParam(required = false) String topic,
                                                           @RequestParam(required = false) int status,
                                                           @RequestParam(required = false) String filterTime) {

        // parse param
        Date effectTimeStart = null;
        Date effectTimeEnd = null;
        if (filterTime!=null && filterTime.trim().length()>0) {
            String[] temp = filterTime.split(" - ");
            if (temp!=null && temp.length == 2) {
                effectTimeStart = DateTool.parseDateTime(temp[0]);
                effectTimeEnd = DateTool.parseDateTime(temp[1]);
            }
        }

        // page query
        PageModel<MessageArchiveDTO> pageModel = messageAichiveService.pageList(offset, pagesize, topic, status, effectTimeStart, effectTimeEnd);
        return Response.ofSuccess(pageModel);
    }


    /**
     * 归档清理
     */
    @RequestMapping("/archiveClean")
    @ResponseBody
    @XxlSso
    public Response<String> archiveClean(String topic, Integer archiveStrategy){
        return messageAichiveService.clean(topic, archiveStrategy);
    }

}
