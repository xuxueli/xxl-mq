package com.xxl.mq.admin.broker.openapi;

import com.xxl.mq.admin.broker.config.BrokerBootstrap;
import com.xxl.mq.core.constant.Const;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.ConsumeRequest;
import com.xxl.mq.core.openapi.model.ProduceRequest;
import com.xxl.mq.core.openapi.model.PullRequest;
import com.xxl.mq.core.openapi.model.RegistryRequest;
import com.xxl.sso.core.annotation.XxlSso;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.response.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@Controller
public class OpenApiController {

    /*@Resource
    private JsonRpcServer jsonRpcServer;*/

    @Resource
    private BrokerService brokerService;

    @RequestMapping("/openapi/{uri}")
    @ResponseBody
    @XxlSso(login = false)
    public Response<?> api(HttpServletRequest request,
                           @PathVariable("uri") String uri,
                           @RequestHeader(Const.XXL_MQ_ACCESS_TOKEN) String accesstoken,
                           @RequestBody(required = false) String requestBody){

        // valid param
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return Response.ofFail("invalid request, HttpMethod not support.");
        }
        if (StringTool.isBlank(uri)) {
            return Response.ofFail("invalid request, uri-mapping empty.");
        }
        if (StringTool.isBlank(requestBody)) {
            return Response.ofFail("invalid request, requestBody empty.");
        }

        // valid token
        if (!BrokerBootstrap.getInstance().getAccessTokenThreadHelper().validAccessToken(accesstoken)) {
            return Response.ofFail("accessToken invalid");
        }

        // dispatch request
        try {
            switch (uri) {
                case "registry":
                    return brokerService.registry(GsonTool.fromJson(requestBody, RegistryRequest.class));
                case "registryRemove":
                    return brokerService.registryRemove(GsonTool.fromJson(requestBody, RegistryRequest.class));
                case "produce":
                    return brokerService.produce(GsonTool.fromJson(requestBody, ProduceRequest.class));
                case "consume":
                    return brokerService.consume(GsonTool.fromJson(requestBody, ConsumeRequest.class));
                case "pullAndLock":
                    return brokerService.pullAndLock(GsonTool.fromJson(requestBody, PullRequest.class));
                case "pullPreCheck":
                    return brokerService.pullPreCheck(GsonTool.fromJson(requestBody, PullRequest.class));
                default:
                    return Response.ofFail("invalid request, uri-mapping invalid.");
            }
            //return jsonRpcServer.invoke(requestBody);
        } catch (Exception e) {
            return Response.ofFail("openapi invoke error: " + e.getMessage());
        }
    }

}
