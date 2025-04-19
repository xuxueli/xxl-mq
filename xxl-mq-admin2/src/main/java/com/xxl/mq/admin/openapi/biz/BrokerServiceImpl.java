package com.xxl.mq.admin.openapi.biz;

import com.xxl.mq.admin.openapi.config.BrokerFactory;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.*;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.response.Response;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * broker service
 *
 * Created by xuxueli on 16/8/28.
 */
@Service
public class BrokerServiceImpl implements BrokerService {


    /**
     * 1、注册请求逻辑：
     *      a、注册请求数据格式：入参【RegistryRequest】格式如下；数据异构为【Instance】，存储在 “xxl_mq_instance”。
     *      <pre>
     *      {
     *          "accessToken":"xxx",
     *          "appname":"xxx",
     *          "instanceUuid":"uuid_01",
     *          "topicList":["topic01", "topic02"]
     *      }
     *      </pre>
     *      b、数据初始化：
     *          - Instance 写入；
     *          - Topic + AppName 新实体生成；
     *
     * 2、本地缓存计算：时机，30s/次；
     *      a、注册数据快照：三次心跳范围内判断 appname维度活跃【Instance】；数据异构为【ApplicationRegistryData】，存储在 “xxl_mq_application#registry_data”；
     *      b、本地缓存：
     *          1、appname 缓存信息：Key 为 appname，Value 包括注册【Instance】信息；
     *          2、topic 缓存信息：Key 为 topic，Value 包含关联的 appname；
     *
     */

    @Override
    public Response<String> registry(RegistryRequest registryRequest) {
        // valid token
        if (!validAccessToken(registryRequest)) {
            return Response.ofFail("accessToken invalid");
        }

        // invoke
        boolean ret = BrokerFactory.getInstance().registry(registryRequest);
        return ret? Response.ofSuccess() : Response.ofFail();
    }

    /**
     * valid accessToken
     */
    private boolean validAccessToken(Object requestParam) {
        if (requestParam == null) {
            return false;
        }
        if (requestParam instanceof BaseRequest) {
            BaseRequest baseRequest = (BaseRequest) requestParam;
            return BrokerFactory.getInstance().validAccessToken(baseRequest.getAccessToken());
        }
        return false;
    }

    @Override
    public Response<String> registryRemove(RegistryRequest registryRequest) {
        // valid token
        if (!validAccessToken(registryRequest)) {
            return Response.ofFail("accessToken invalid");
        }

        // invoke
        boolean ret = BrokerFactory.getInstance().registryRemove(registryRequest);
        return ret? Response.ofSuccess() : Response.ofFail();
    }

    @Override
    public Response<String> produce(ProduceRequest produceRequest) {
        // valid token
        if (!validAccessToken(produceRequest)) {
            return Response.ofFail("accessToken invalid");
        }

        // todo
        return null;
    }

    @Override
    public Response<List<MessageData>> pull(PullRequest pullRequest) {
        // valid token
        if (!validAccessToken(pullRequest)) {
            return Response.ofFail("accessToken invalid");
        }

        return null;
    }

    @Override
    public Response<String> consume(ConsumeRequest consumeRequest) {
        // valid token
        if (!validAccessToken(consumeRequest)) {
            return Response.ofFail("accessToken invalid");
        }

        return null;
    }
}
