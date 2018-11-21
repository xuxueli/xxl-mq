package com.xxl.mq.admin.controller;

import com.xxl.mq.admin.core.model.XxlMqBiz;
import com.xxl.mq.admin.core.model.XxlMqTopic;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.service.IXxlMqBizService;
import com.xxl.mq.admin.service.IXxlMqTopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author xuxueli 2018-11-21
 */
@Controller
@RequestMapping("/topic")
public class TopicController {

    @Resource
    private IXxlMqTopicService xxlMqTopicService;
    @Resource
    private IXxlMqBizService xxlMqBizService;


    @RequestMapping("")
    public String index(Model model){

        List<XxlMqBiz> bizList = xxlMqBizService.findAll();
        model.addAttribute("bizList", bizList);

        return "topic/topic.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        int bizId,
                                        String topic){
        return xxlMqTopicService.pageList(start, length, bizId, topic);
    }

    @RequestMapping("/delete")
    @ResponseBody
    public ReturnT<String> delete(String topic){
        return xxlMqTopicService.delete(topic);
    }

    @RequestMapping("/update")
    @ResponseBody
    public ReturnT<String> update(XxlMqTopic xxlMqTopic){
        return xxlMqTopicService.update(xxlMqTopic);
    }

    @RequestMapping("/add")
    @ResponseBody
    public ReturnT<String> add(XxlMqTopic xxlMqTopic){
        return xxlMqTopicService.add(xxlMqTopic);
    }

}
