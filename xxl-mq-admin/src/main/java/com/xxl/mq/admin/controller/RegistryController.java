package com.xxl.mq.admin.controller;

import com.xxl.mq.admin.controller.annotation.PermessionLimit;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.service.XxlCommonRegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * xxl native regsitry, borrowed from "xxl-rpc"
 *
 * @author xuxueli 2018-11-26
 */
@Controller
@RequestMapping("/registry")
public class RegistryController {


    @Resource
    private XxlCommonRegistryService xxlCommonRegistryService;


    @RequestMapping("/registry")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> registry(HttpServletRequest request,  @RequestParam(name = "keys", required = false) List<String> keys, String value){
        return xxlCommonRegistryService.registry(keys, value);
    }

    @RequestMapping("/remove")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> remove(@RequestParam(name = "keys", required = false) List<String> keys, String value){
        return xxlCommonRegistryService.remove(keys, value);
    }

    @RequestMapping("/discovery")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<Map<String, List<String>>> discovery(@RequestParam(name = "keys", required = false) List<String> keys) {
        return xxlCommonRegistryService.discovery(keys);
    }

    @RequestMapping("/monitor")
    @ResponseBody
    @PermessionLimit(limit=false)
    public DeferredResult monitor(@RequestParam(name = "keys", required = false) List<String> keys) {
        return xxlCommonRegistryService.monitor(keys);
    }

}
