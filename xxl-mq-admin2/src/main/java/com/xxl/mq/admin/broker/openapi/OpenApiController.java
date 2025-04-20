package com.xxl.mq.admin.broker.openapi;

import com.xxl.mq.admin.annotation.Permission;
import com.xxl.tool.jsonrpc.JsonRpcServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
public class OpenApiController {

    @Resource
    private JsonRpcServer jsonRpcServer;

    @RequestMapping("/openapi")
    @ResponseBody
    @Permission(login = false)
    public String api(@RequestBody(required = false) String requestBody){
        return jsonRpcServer.invoke(requestBody);
    }

}
