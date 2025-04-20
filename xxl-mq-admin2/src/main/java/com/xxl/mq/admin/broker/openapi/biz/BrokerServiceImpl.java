package com.xxl.mq.admin.broker.openapi.biz;

import com.xxl.mq.admin.broker.config.BrokerFactory;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.*;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.Response;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * broker service
 *
 * Created by xuxueli on 16/8/28.
 */
@Service
public class BrokerServiceImpl implements BrokerService {

    @Resource
    private BrokerFactory instance;

    /**
     * 1、注册请求逻辑：
     *      a、注册请求数据格式：入参【RegistryRequest】格式如下；数据异构为【Instance】，存储在 “xxl_mq_instance”。
     *      <pre> // 请求
     *      {
     *          "accessToken":"xxx",
     *          "appname":"xxx",
     *          "instanceUuid":"uuid_01",
     *          "topicList":["topic01", "topic02"]
     *      }
     *      </pre>
     *      <pre> // 响应
     *      {
     *          "code":200,
     *          "msg":"success"
     *      }
     *      </pre>
     *      b、数据初始化：
     *          - Instance 写入；
     *          - Topic + AppName 新实体生成；
     *
     * 2、注册数据快照 + 本地缓存计算：时机，30s/次；
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
        if (StringTool.isBlank(registryRequest.getAppname()) || StringTool.isBlank(registryRequest.getInstanceUuid())) {
            return Response.ofFail("appname or instanceUuid is empty.");
        }

        // invoke
        boolean ret = BrokerFactory.getInstance().getRegistryMessageQueueHelper().registry(registryRequest);
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
            return BrokerFactory.getInstance().getAccessTokenThreadHelper().validAccessToken(baseRequest.getAccessToken());
        }
        return false;
    }

    /**
     * 2、注册摘除请求逻辑：入参【RegistryRequest】格式如下；影响注册数据快照。
     *      <pre> // 请求
     *      {
     *          "accessToken":"xxx",
     *          "appname":"xxx",
     *          "instanceUuid":"uuid_01"
     *      }
     *      </pre>
     *      <pre> // 响应
     *      {
     *          "code":200,
     *          "msg":"success"
     *      }
     *      </pre>
     */
    @Override
    public Response<String> registryRemove(RegistryRequest registryRequest) {
        // valid token
        if (!validAccessToken(registryRequest)) {
            return Response.ofFail("accessToken invalid");
        }
        if (StringTool.isBlank(registryRequest.getAppname()) || StringTool.isBlank(registryRequest.getInstanceUuid())) {
            return Response.ofFail("appname or instanceUuid is empty.");
        }

        // invoke
        boolean ret = BrokerFactory.getInstance().getRegistryMessageQueueHelper().registryRemove(registryRequest);
        return ret? Response.ofSuccess() : Response.ofFail();
    }

    /**
     * 3、生产请求逻辑：入参【ProduceRequest】格式如下；影响注册数据快照。
     *      <pre> // 请求
     *          {
     *              "messageList":[{
     *                  "topic":"topic01",
     *                  "partitionKey":"{分区Key，用于分区路由}",
     *                  "data":"{消息数据}",
     *                  "effectTime":{1234567890, 时间戳，毫秒}
     *              }]
     *          }
     *      </pre>
     *      <pre> // 响应
     *      {
     *          "code":200,
     *          "msg":"success"
     *      }
     *      </pre>
     */
    @Override
    public Response<String> produce(ProduceRequest produceRequest) {
        // valid token
        if (!validAccessToken(produceRequest)) {
            return Response.ofFail("accessToken invalid");
        }

        // invoke
        boolean ret = BrokerFactory.getInstance().getMessageHelper().produce(produceRequest);
        return ret? Response.ofSuccess() : Response.ofFail();
    }

    /**
     * 4、消费请求逻辑：入参【ConsumeRequest】格式如下；影响注册数据快照。
     *      <pre> // 请求
     *          {
     *              "messageList":[{
     *                  "id":{111, 消息ID},
     *                  "status":{2, 消息状态值，参考枚举：MessageStatusEnum },
     *                  "consumeLog":{消息消费流水日志},
     *              }]
     *          }
     *      </pre>
     *      <pre> // 响应
     *      {
     *          "code":200,
     *          "msg":"success"
     *      }
     *      </pre>
     */
    @Override
    public Response<String> consume(ConsumeRequest consumeRequest) {
        // valid token
        if (!validAccessToken(consumeRequest)) {
            return Response.ofFail("accessToken invalid");
        }

        // invoke
        boolean ret = BrokerFactory.getInstance().getMessageHelper().consume(consumeRequest);
        return ret? Response.ofSuccess() : Response.ofFail();
    }

    /**
     * 5、拉取消息请求逻辑：入参【PullRequest】格式如下；影响注册数据快照。
     *      <pre> // 请求
     *          {
     *              "appname":"{appname}",
     *              "instanceUuid":"{分区Key，用于分区路由}",
     *              "topicList":["topic01", "topic02"]
     *          }
     *      </pre>
     *      <pre> // 响应
     *      {
     *          "code":200,
     *          "msg":"success",
     *          "data":[{
     *                  "topic":"topic01",
     *                  "partitionKey":"{分区Key，用于分区路由}",
     *                  "data":"{消息数据}",
     *                  "effectTime":{1234567890, 时间戳，毫秒}
     *          }]
     *      </pre>
     */
    @Override
    public Response<List<MessageData>> pull(PullRequest pullRequest) {
        // valid token
        if (!validAccessToken(pullRequest)) {
            return Response.ofFail("accessToken invalid");
        }

        // invoke
        return BrokerFactory.getInstance().getMessageHelper().pull(pullRequest);
    }

}
