package com.xxl.mq.admin.controller.biz;

import com.xxl.mq.admin.constant.enums.*;
import com.xxl.mq.admin.model.entity.Application;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.service.ApplicationService;
import com.xxl.mq.admin.service.TopicService;
import com.xxl.mq.admin.util.I18nUtil;
import com.xxl.sso.core.annotation.XxlSso;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import com.xxl.tool.response.Response;
import com.xxl.tool.response.PageModel;

import static com.xxl.mq.admin.controller.biz.MessageArchiveController.findPermissionApplication;

/**
* Topic Controller
*
* Created by xuxueli on '2025-03-21 12:52:25'.
*/
@Controller
@RequestMapping("/topic")
public class TopicController {

    @Resource
    private TopicService topicService;
    @Resource
    private ApplicationService applicationService;

    /**
    * 页面
    */
    @RequestMapping
    @XxlSso
    public String index(Model model, HttpServletRequest request) {

        // enum
        model.addAttribute("StoreStrategyEnum", StoreStrategyEnum.values());
        model.addAttribute("PartitionRouteStrategyEnum", PartitionRouteStrategyEnum.values());
        model.addAttribute("ArchiveStrategyEnum", ArchiveStrategyEnum.values());
        model.addAttribute("TopicLevelStrategyEnum", TopicLevelStrategyEnum.values());
        model.addAttribute("RetryStrategyEnum", RetryStrategyEnum.values());

        // appname
        List<Application> applicationList = findPermissionApplication(request, applicationService);
        model.addAttribute("applicationList", applicationList);

        return "biz/topic";
    }

    /**
     * has permission for appname
     *
     * @param request
     * @param appname
     * @return
     */
    private boolean hasPermissionForAppname(HttpServletRequest request, String appname){
        List<Application> applicationList = findPermissionApplication(request, applicationService);
        return applicationList.stream()
                .map(Application::getAppname)
                .collect(Collectors.toList())
                .contains(appname);
    }

    /**
    * 分页查询
    */
    @RequestMapping("/pageList")
    @ResponseBody
    @XxlSso
    public Response<PageModel<Topic>> pageList(@RequestParam(required = false, defaultValue = "0") int offset,
                                               @RequestParam(required = false, defaultValue = "10") int pagesize,
                                               @RequestParam(required = false) String appname,
                                               @RequestParam(required = false) String topic) {
        PageModel<Topic> pageModel = topicService.pageList(offset, pagesize, appname, topic);
        return Response.ofSuccess(pageModel);
    }

    /**
    * Load查询
    */
    @RequestMapping("/load")
    @ResponseBody
    @XxlSso
    public Response<Topic> load(int id){
        return topicService.load(id);
    }

    /**
    * 新增
    */
    @RequestMapping("/insert")
    @ResponseBody
    @XxlSso
    public Response<String> insert(HttpServletRequest request, Topic topic){
        // valid
        if (!hasPermissionForAppname(request, topic.getAppname())) {
            return Response.ofFail(I18nUtil.getString("system_permission_limit"));
        }

        return topicService.insert(topic);
    }

    /**
    * 删除
    */
    @RequestMapping("/delete")
    @ResponseBody
    @XxlSso
    public Response<String> delete(HttpServletRequest request, @RequestParam("ids[]") List<Integer> ids){
        return topicService.delete(ids);
    }

    /**
    * 更新
    */
    @RequestMapping("/update")
    @ResponseBody
    @XxlSso
    public Response<String> update(HttpServletRequest request, Topic topic){
        // valid
        if (!hasPermissionForAppname(request, topic.getAppname())) {
            return Response.ofFail(I18nUtil.getString("system_permission_limit"));
        }

        return topicService.update(topic);
    }

    /**
     * 修改状态
     */
    @RequestMapping("/updateStatus")
    @ResponseBody
    @XxlSso
    public Response<String> updateStatus(HttpServletRequest request,
                                         @RequestParam("ids[]") List<Integer> ids,
                                         @RequestParam int status){
        return topicService.updateStatus(ids, status);
    }

}
