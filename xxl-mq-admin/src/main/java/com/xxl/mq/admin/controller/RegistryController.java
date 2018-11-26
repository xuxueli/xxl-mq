package com.xxl.mq.admin.controller;

import com.xxl.mq.admin.controller.annotation.PermessionLimit;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.service.XxlMqRegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;

/**
 * @author xuxueli 2018-11-26
 */
@Controller
@RequestMapping("/registry")
public class RegistryController {


    private XxlMqRegistryService xxlMqRegistryService;


    @RequestMapping("/registry")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> registry(String biz, String env, @RequestParam(name = "keys", required = false) List<String> keys, String value){
        return xxlMqRegistryService.registry(biz, env, keys, value);
    }

    @RequestMapping("/remove")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> remove(String biz, String env, @RequestParam(name = "keys", required = false) List<String> keys, String value){
        return xxlMqRegistryService.remove(biz, env, keys, value);
    }

    @RequestMapping("/discovery")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<Map<String, List<String>>> discovery(String biz, String env, @RequestParam(name = "keys", required = false) List<String> keys) {
        return xxlMqRegistryService.discovery(biz, env, keys);
    }

    @RequestMapping("/monitor")
    @ResponseBody
    @PermessionLimit(limit=false)
    public DeferredResult monitor(String biz, String env, @RequestParam(name = "keys", required = false) List<String> keys) {
        return xxlMqRegistryService.monitor(biz, env, keys);
    }

}
