package com.xxl.mq.broker.controller;

import com.xxl.mq.broker.controller.annotation.PermessionLimit;
import com.xxl.mq.broker.core.model.XxlMqBiz;
import com.xxl.mq.broker.service.IXxlMqBizService;
import com.xxl.mq.broker.service.IXxlMqTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

@Controller
@RequestMapping("/topic")
public class TopicController {

    @Resource
    private IXxlMqTopicService xxlMqTopicService;
    @Resource
    private IXxlMqBizService xxlMqBizService;


    @RequestMapping("")
    @PermessionLimit
    public String index(Model model){

        List<XxlMqBiz> bizList = xxlMqBizService.findAll();
        model.addAttribute("bizList", bizList);

        return "topic/topic.index";
    }

    /**
     * TODO
     */

}
