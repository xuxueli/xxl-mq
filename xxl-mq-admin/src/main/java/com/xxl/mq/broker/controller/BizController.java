package com.xxl.mq.broker.controller;

import com.xxl.mq.broker.service.IXxlMqBizService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@Controller
@RequestMapping("/biz")
public class BizController {

    @Resource
    private IXxlMqBizService xxlMqBizService;



}
