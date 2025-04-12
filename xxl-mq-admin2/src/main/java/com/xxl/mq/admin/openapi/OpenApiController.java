package com.xxl.mq.admin.openapi;

import com.xxl.mq.admin.annotation.Permission;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.tool.jsonrpc.JsonRpcServer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Controller
public class OpenApiController {

    /**
     * json-rpc server
     */
    private JsonRpcServer jsonRpcServer;
    @Resource
    private BrokerService brokerService;

    @PostConstruct
    public void initJsonRpcServer() {
        jsonRpcServer = new JsonRpcServer();
        jsonRpcServer.register("brokerService", brokerService);
    }

    @RequestMapping("/openapi")
    @ResponseBody
    @Permission(login = false)
    public String api(@RequestBody(required = false) String requestBody){
        return jsonRpcServer.invoke(requestBody);
    }

}
