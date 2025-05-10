package com.xxl.mq.admin.controller.biz;

import com.xxl.mq.admin.annotation.Permission;
import com.xxl.mq.admin.constant.enums.ArchiveStrategyEnum;
import com.xxl.mq.admin.constant.enums.MessageStatusEnum;
import com.xxl.mq.admin.constant.enums.TopicStatusEnum;
import com.xxl.mq.admin.model.adaptor.MessageAdaptor;
import com.xxl.mq.admin.model.dto.LoginUserDTO;
import com.xxl.mq.admin.model.dto.MessageDTO;
import com.xxl.mq.admin.model.entity.Application;
import com.xxl.mq.admin.model.entity.Message;
import com.xxl.mq.admin.service.ApplicationService;
import com.xxl.mq.admin.service.MessageService;
import com.xxl.mq.admin.service.impl.LoginService;
import com.xxl.tool.core.DateTool;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.xxl.tool.response.Response;
import com.xxl.tool.response.PageModel;

/**
* Message Controller
*
* Created by xuxueli on '2025-03-21 21:54:06'.
*/
@Controller
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;
    @Resource
    private ApplicationService applicationService;
    @Resource
    private LoginService loginService;

    /**
    * 页面
    */
    @RequestMapping
    @Permission
    public String index(Model model, HttpServletRequest request, String topic) {

        // Enum
        model.addAttribute("MessageStatusEnum", MessageStatusEnum.values());
        model.addAttribute("ArchiveStrategyEnum", ArchiveStrategyEnum.values());

        // appname
        List<Application> applicationList = findPermissionApplication(request);
        model.addAttribute("applicationList", applicationList);

        // param
        model.addAttribute("topic", topic);

        return "biz/message";
    }

    /**
     * find permission application list
     * @param request
     * @return
     */
    private List<Application> findPermissionApplication(HttpServletRequest request){
        List<Application> applicationList = applicationService.findAll().getData();
        if (!loginService.isAdmin(request)) {
            LoginUserDTO loginUser = loginService.getLoginUser(request);
            List<String> appnameList = loginUser.getPermission()!=null? Arrays.asList(loginUser.getPermission().split(",")):new ArrayList<>();
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
    @Permission
    public Response<PageModel<MessageDTO>> pageList(@RequestParam(required = false, defaultValue = "0") int offset,
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
        PageModel<MessageDTO> pageModel = messageService.pageList(offset, pagesize, topic, status, effectTimeStart, effectTimeEnd);
        return Response.ofSuccess(pageModel);
    }

    /**
    * Load查询
    */
    @RequestMapping("/load")
    @ResponseBody
    @Permission
    public Response<Message> load(int id){
        return messageService.load(id);
    }

    /**
    * 新增
    */
    @RequestMapping("/insert")
    @ResponseBody
    @Permission
    public Response<String> insert(MessageDTO messageDTO){
        return messageService.insert(messageDTO);
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    @ResponseBody
    @Permission
    public Response<String> delete(@RequestParam("ids[]") List<Long> ids){
        return messageService.delete(ids);
    }

    /**
    * 更新
    */
    @RequestMapping("/update")
    @ResponseBody
    @Permission
    public Response<String> update(MessageDTO messageDTO){
        return messageService.update(messageDTO);
    }

    /**
     * 更新
     */
    @RequestMapping("/archive")
    @ResponseBody
    @Permission
    public Response<String> archive(String topic, Integer archiveStrategy){
        return messageService.archive(topic, archiveStrategy, 100);
    }

}
