package com.xxl.mq.broker.controller;

import com.xxl.mq.broker.controller.annotation.PermessionLimit;
import com.xxl.mq.broker.core.model.XxlMqBiz;
import com.xxl.mq.broker.core.result.ReturnT;
import com.xxl.mq.broker.service.IXxlMqBizService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xuxueli 2018-11-20
 */
@Controller
@RequestMapping("/biz")
public class BizController {

    @Resource
    private IXxlMqBizService xxlMqBizService;

    @RequestMapping("")
    @PermessionLimit
    public String index(Model model){

        List<XxlMqBiz> bizList = xxlMqBizService.findAll();
        model.addAttribute("bizList", bizList);

        return "biz/biz.index";
    }

    @RequestMapping("/save")
    @ResponseBody
    public ReturnT<String> save(XxlMqBiz xxlMqBiz){
        return xxlMqBizService.add(xxlMqBiz);
    }

    @RequestMapping("/update")
    @ResponseBody
    public ReturnT<String> update(XxlMqBiz xxlMqBiz){
        return xxlMqBizService.update(xxlMqBiz);
    }


    @RequestMapping("/remove")
    @ResponseBody
    public ReturnT<String> remove(int id){
        return xxlMqBizService.delete(id);
    }

}
