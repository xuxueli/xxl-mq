package com.xxl.mq.admin.controller;

import com.xxl.mq.admin.controller.annotation.PermessionLimit;
import com.xxl.mq.admin.core.model.XxlCommonRegistryData;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.core.util.JacksonUtil;
import com.xxl.mq.admin.service.XxlCommonRegistryService;
import com.xxl.registry.client.model.XxlRegistryDataParamVO;
import com.xxl.registry.client.model.XxlRegistryParamVO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * common regsitry, borrowed from "xxl-registry"
 *
 * @author xuxueli 2018-11-29
 */
@Controller
@RequestMapping("/api")
public class ApiController {

    @Resource
    private XxlCommonRegistryService xxlCommonRegistryService;


    /**
     * 服务注册 & 续约 API
     *
     * 说明：新服务注册上线1s内广播通知接入方；需要接入方循环续约，否则服务将会过期（三倍于注册中心心跳时间）下线；
     *
     * ------
     * 地址格式：{服务注册中心跟地址}/registry
     *
     * 请求参数说明：
     *  1、accessToken：请求令牌；
     *  2、biz：业务标识
     *  2、env：环境标识
     *  3、registryDataList：服务注册信息
     *
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     *
     *     {
     *         "accessToken" : "xx",
     *         "biz" : "xx",
     *         "env" : "xx",
     *         "registryDataList" : [{
     *             "key" : "service01",
     *             "value" : "address01"
     *         }]
     *     }
     *
     * @param data
     * @return
     */
    @RequestMapping("/registry")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> registry(@RequestBody(required = false) String data){

        // parse data
        XxlRegistryParamVO registryParamVO = null;
        try {
            registryParamVO = (XxlRegistryParamVO) JacksonUtil.readValue(data, XxlRegistryParamVO.class);
        } catch (Exception e) { }

        // parse param
        String accessToken = null;
        String biz = null;
        String env = null;
        List<XxlCommonRegistryData> registryDataList = null;

        if (registryParamVO != null) {
            accessToken = registryParamVO.getAccessToken();
            biz = registryParamVO.getBiz();
            env = registryParamVO.getEnv();
            if (registryParamVO.getRegistryDataList()!=null) {
                registryDataList = new ArrayList<>();
                for (XxlRegistryDataParamVO dataParamVO: registryParamVO.getRegistryDataList()) {
                    XxlCommonRegistryData dateItem = new XxlCommonRegistryData();
                    dateItem.setKey(dataParamVO.getKey());
                    dateItem.setValue(dataParamVO.getValue());
                    registryDataList.add(dateItem);
                }
            }
        }

        return xxlCommonRegistryService.registry(accessToken, registryDataList);
    }

    /**
     * 服务摘除 API
     *
     * 说明：新服务摘除下线1s内广播通知接入方；
     *
     * ------
     * 地址格式：{服务注册中心跟地址}/remove
     *
     * 请求参数说明：
     *  1、accessToken：请求令牌；
     *  2、biz：业务标识
     *  2、env：环境标识
     *  3、registryDataList：服务注册信息
     *
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     *
     *     {
     *         "accessToken" : "xx",
     *         "biz" : "xx",
     *         "env" : "xx",
     *         "registryDataList" : [{
     *             "key" : "service01",
     *             "value" : "address01"
     *         }]
     *     }
     *
     * @param data
     * @return
     */
    @RequestMapping("/remove")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<String> remove(@RequestBody(required = false) String data){

        // parse data
        XxlRegistryParamVO registryParamVO = null;
        try {
            registryParamVO = (XxlRegistryParamVO) JacksonUtil.readValue(data, XxlRegistryParamVO.class);
        } catch (Exception e) { }

        // parse param
        String accessToken = null;
        String biz = null;
        String env = null;
        List<XxlCommonRegistryData> registryDataList = null;

        if (registryParamVO != null) {
            accessToken = registryParamVO.getAccessToken();
            biz = registryParamVO.getBiz();
            env = registryParamVO.getEnv();
            if (registryParamVO.getRegistryDataList()!=null) {
                registryDataList = new ArrayList<>();
                for (XxlRegistryDataParamVO dataParamVO: registryParamVO.getRegistryDataList()) {
                    XxlCommonRegistryData dateItem = new XxlCommonRegistryData();
                    dateItem.setKey(dataParamVO.getKey());
                    dateItem.setValue(dataParamVO.getValue());
                    registryDataList.add(dateItem);
                }
            }
        }

        return xxlCommonRegistryService.remove(accessToken, registryDataList);
    }


    /**
     * 服务发现 API
     *
     * 说明：查询在线服务地址列表；
     *
     * ------
     * 地址格式：{服务注册中心跟地址}/discovery
     *
     * 请求参数说明：
     *  1、accessToken：请求令牌；
     *  2、biz：业务标识
     *  2、env：环境标识
     *  3、keys：服务注册Key列表
     *
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     *
     *     {
     *         "accessToken" : "xx",
     *         "biz" : "xx",
     *         "env" : "xx",
     *         "keys" : [
     *             "service01",
     *             "service02"
     *         ]
     *     }
     *
     * @param data
     * @return
     */
    @RequestMapping("/discovery")
    @ResponseBody
    @PermessionLimit(limit=false)
    public ReturnT<Map<String, List<String>>> discovery(@RequestBody(required = false) String data) {

        // parse data
        XxlRegistryParamVO registryParamVO = null;
        try {
            registryParamVO = (XxlRegistryParamVO) JacksonUtil.readValue(data, XxlRegistryParamVO.class);
        } catch (Exception e) { }

        // parse param
        String accessToken = null;
        String biz = null;
        String env = null;
        List<String> keys = null;

        if (registryParamVO != null) {
            accessToken = registryParamVO.getAccessToken();
            biz = registryParamVO.getBiz();
            env = registryParamVO.getEnv();
            keys = registryParamVO.getKeys();
        }

        return xxlCommonRegistryService.discovery(accessToken, keys);
    }

    /**
     * 服务监控 API
     *
     * 说明：long-polling 接口，主动阻塞一段时间（三倍于注册中心心跳时间）；直至阻塞超时或服务注册信息变动时响应；
     *
     * ------
     * 地址格式：{服务注册中心跟地址}/monitor
     *
     * 请求参数说明：
     *  1、accessToken：请求令牌；
     *  2、biz：业务标识
     *  2、env：环境标识
     *  3、keys：服务注册Key列表
     *
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     *
     *     {
     *         "accessToken" : "xx",
     *         "biz" : "xx",
     *         "env" : "xx",
     *         "keys" : [
     *             "service01",
     *             "service02"
     *         ]
     *     }
     *
     * @param data
     * @return
     */
    @RequestMapping("/monitor")
    @ResponseBody
    @PermessionLimit(limit=false)
    public DeferredResult monitor(@RequestBody(required = false) String data) {

        // parse data
        XxlRegistryParamVO registryParamVO = null;
        try {
            registryParamVO = (XxlRegistryParamVO) JacksonUtil.readValue(data, XxlRegistryParamVO.class);
        } catch (Exception e) { }

        // parse param
        String accessToken = null;
        String biz = null;
        String env = null;
        List<String> keys = null;

        if (registryParamVO != null) {
            accessToken = registryParamVO.getAccessToken();
            biz = registryParamVO.getBiz();
            env = registryParamVO.getEnv();
            keys = registryParamVO.getKeys();
        }

        return xxlCommonRegistryService.monitor(accessToken, keys);
    }

}
