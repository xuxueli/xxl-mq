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
     *      a、注册请求数据格式：
     *      <pre>   //  【RegistryRequest】
     *      {
     *          "accessToken":"xxx",
     *          "appname":"xxx",
     *          "instanceUuid":"uuid_01",
     *          "topicGroup":{
     *              "topic_01":["default"],
     *              "topic_02":["uuid_01", "uuid_02"]
     *          {
     *      }
     *      </pre>
     *      b、注册请求数据存储：存储在 “xxl_mq_instance” 表；appname 与 instanceUuid 以及心跳时间单独存储，topicGroup存储在 registry_data。
     *      <pre>   // 【RegistryRequest】
     *      {
     *          "appname":"xxx",
     *          "topicGroup":{
     *              "topic_01":["default"],
     *              "topic_02":["uuid_01", "uuid_02"]
     *          {
     *      }
     *      </pre>
     *
     * 2、注册数据定期计算逻辑：
     *      1、新实体生成：appname、topic，自动初始化；自动建立关联关系；若存则忽略；
     *      2、注册信息计算：30s/次，三次心跳范围内判断instance活跃；appname 维度聚合计算，同步至 “xxl_mq_application#registry_data”；（更新时过滤重复更新；）
     *      3、注册信息数据结构：
     *      <pre>   // 【ApplicationRegistryData】
     *      {
     *          "instancePartitionRange":{           // 根据活跃 instance，计算获取；
     *              "instanceUuid_01": {
     *                  "partitionFrom": 0,
     *                  "partitionTo": 5000
     *              },
     *              "instanceUuid_02": {
     *                  "partitionFrom": 5001,
     *                  "partitionTo": 10000
     *              }
     *          },
     *          "topicGroup":{                       // 默认同appname下instance的topic一致；取最近心跳 instance 的topic数据；
     *              "topic_01":["default"],
     *              "topic_02":["uuid_01", "uuid_02"]
     *          {
     *      }
     *      </pre>
     *
     * 3、本次缓存计算逻辑：
     *      1、更新时机：30s/次；在上述 2 执行后，立即更新缓存；
     *      2、数据数据：
     *          a、appname 缓存信息：Key 为 appname，Value 为上文 “ApplicationRegistryData”
     *          b、topic 缓存信息：Key 为 topic，Value 为数据库中 topic 数据（包含关联 appname）；
     *
     */

    @Override
    public Response<String> registry(RegistryRequest registryRequest) {
        // valid token
        if (registryRequest==null || !BrokerFactory.getInstance().validAccessToken(registryRequest.getAccessToken())) {
            return Response.ofFail("accessToken invalid");
        }

        // invoke
        boolean ret = BrokerFactory.getInstance().registry(registryRequest);
        return ret? Response.ofSuccess() : Response.ofFail();
    }

    @Override
    public Response<String> produce(ProduceRequest produceRequest) {
        // todo
        return null;
    }

    @Override
    public Response<List<MessageData>> pull(PullRequest pullRequest) {
        return null;
    }

    @Override
    public Response<String> consumeRequest(ConsumeRequest consumeRequest) {
        return null;
    }
}
