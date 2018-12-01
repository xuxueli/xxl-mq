package com.xxl.mq.admin.controller;

import com.xxl.mq.admin.controller.annotation.PermessionLimit;
import com.xxl.mq.admin.core.model.XxlCommonRegistryData;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.core.util.JacksonUtil;
import com.xxl.mq.admin.service.XxlCommonRegistryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * xxl native regsitry, borrowed from "xxl-registry"
 *
 * @author xuxueli 2018-11-26
 */
@Controller
@RequestMapping("/api")
public class RegistryController {


    @Resource
    private XxlCommonRegistryService xxlCommonRegistryService;


    @RequestMapping("/registry/{biz}/{env}")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> registry(@PathVariable(value = "biz", required = false) String biz,
                                    @PathVariable(value = "env",required = false) String env,
                                    @RequestBody(required = false) String data){

        // parse
        List<XxlCommonRegistryData> xxlCommonRegistryDataList = null;
        try {
            xxlCommonRegistryDataList = (List<XxlCommonRegistryData>) JacksonUtil.readValue(data, List.class, XxlCommonRegistryData.class);
        } catch (Exception e) { }

        return xxlCommonRegistryService.registry(xxlCommonRegistryDataList);
    }

    @RequestMapping("/remove/{biz}/{env}")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> remove(@PathVariable(value = "biz", required = false) String biz,
                                  @PathVariable(value = "env",required = false) String env,
                                  @RequestBody(required = false) String data){

        // parse
        List<XxlCommonRegistryData> xxlCommonRegistryDataList = null;
        try {
            xxlCommonRegistryDataList = (List<XxlCommonRegistryData>) JacksonUtil.readValue(data, List.class, XxlCommonRegistryData.class);
        } catch (Exception e) { }

        return xxlCommonRegistryService.remove(xxlCommonRegistryDataList);
    }

    @RequestMapping("/discovery/{biz}/{env}")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<Map<String, List<String>>> discovery(@PathVariable(value = "biz", required = false) String biz,
                                                        @PathVariable(value = "env",required = false) String env,
                                                        @RequestBody(required = false) String data) {

        List<String> keys = JacksonUtil.readValue(data, List.class);

        return xxlCommonRegistryService.discovery(keys);
    }

    @RequestMapping("/monitor/{biz}/{env}")
    @ResponseBody
    @PermessionLimit(limit=false)
    public DeferredResult monitor(@PathVariable(value = "biz", required = false) String biz,
                                  @PathVariable(value = "env",required = false) String env,
                                  @RequestBody(required = false) String data) {

        List<String> keys = JacksonUtil.readValue(data, List.class);

        return xxlCommonRegistryService.monitor(keys);
    }

}
