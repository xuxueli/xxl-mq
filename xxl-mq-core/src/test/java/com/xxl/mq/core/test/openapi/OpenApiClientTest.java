package com.xxl.mq.core.test.openapi;

import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.ConsumeRequest;
import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.mq.core.openapi.model.ProduceRequest;
import com.xxl.mq.core.openapi.model.RegistryRequest;
import com.xxl.mq.core.util.ConsumeLogUtil;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.http.IPTool;
import com.xxl.tool.jsonrpc.JsonRpcClient;
import com.xxl.tool.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OpenApiClientTest {
    private static Logger logger = LoggerFactory.getLogger(OpenApiClientTest.class);

    private static String url = "http://127.0.0.1:8080/xxl-mq-admin/openapi";
    private static String service = "brokerService";
    private static String accessToken = "defaultaccesstoken";

    private BrokerService buildClient(){
        JsonRpcClient jsonRpcClient = new JsonRpcClient(url, 3 * 1000);
        BrokerService brokerService = jsonRpcClient.proxy(service, BrokerService.class);
        return brokerService;
    }

    @Test
    public void registryTest() {
        // client
        BrokerService brokerService = buildClient();

        // 01
        {
            // param
            RegistryRequest registryRequest = new RegistryRequest();
            registryRequest.setAccessToken("defaultaccesstoken");
            registryRequest.setAppname("xxl-mq-sample");
            registryRequest.setInstanceUuid("uuid_01");
            registryRequest.setTopicList(Arrays.asList("topic_sample", "topic_sample02"));

            // invoke
            Response<String> response = brokerService.registry(registryRequest);
            logger.info("response:{}", response);
        }

        // 02
        {
            // param
            RegistryRequest registryRequest = new RegistryRequest();
            registryRequest.setAccessToken("defaultaccesstoken");
            registryRequest.setAppname("xxl-mq-sample");
            registryRequest.setInstanceUuid("uuid_02");

            // invoke
            Response<String> response = brokerService.registry(registryRequest);
            logger.info("response:{}", response);
        }

        // 02
        {
            // param
            RegistryRequest registryRequest = new RegistryRequest();
            registryRequest.setAccessToken("defaultaccesstoken");
            registryRequest.setAppname("xxl-mq-sample");
            registryRequest.setInstanceUuid("uuid_03");

            // invoke
            Response<String> response = brokerService.registry(registryRequest);
            logger.info("response:{}", response);
        }
    }

    @Test
    public void registryRemoveTest() throws InterruptedException {
        // client
        BrokerService brokerService = buildClient();

        // param
        RegistryRequest registryRequest = new RegistryRequest();
        registryRequest.setAccessToken("defaultaccesstoken");
        registryRequest.setAppname("xxl-mq-sample");
        registryRequest.setInstanceUuid("uuid_02");

        // invoke
        Response<String> response = brokerService.registryRemove(registryRequest);
        logger.info("response:{}", response);
    }

    @Test
    public void produceTest() {
        // client
        BrokerService brokerService = buildClient();

        ProduceRequest produceRequest = new ProduceRequest();
        produceRequest.setAccessToken("defaultaccesstoken");
        produceRequest.setMessageList(Arrays.asList(
                new MessageData("topic_sample", "pk-111", "data111", System.currentTimeMillis()),
                new MessageData("topic_sample", "pk-111", "data222", System.currentTimeMillis()+1000),
                new MessageData("topic_sample", "pk-222", "data333", System.currentTimeMillis()+2000)
        ));

        Response<String> response = brokerService.produce(produceRequest);
        logger.info("response:{}", response);
    }

    @Test
    public void produce2Test() {
        // client
        BrokerService brokerService = buildClient();

        ProduceRequest produceRequest = new ProduceRequest();
        produceRequest.setAccessToken("defaultaccesstoken");
        produceRequest.setMessageList(Arrays.asList(
                new MessageData("topic_sample02", "pk-222", "data333", System.currentTimeMillis()+2000)
        ));

        Response<String> response = brokerService.produce(produceRequest);
        logger.info("response:{}", response);
    }

    @Test
    public void produce3Test() {
        // client
        BrokerService brokerService = buildClient();

        // send 10W
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            ProduceRequest produceRequest = new ProduceRequest();
            produceRequest.setAccessToken("defaultaccesstoken");
            produceRequest.setMessageList(Arrays.asList(
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-1000", System.currentTimeMillis()+1000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-2000", System.currentTimeMillis()+2000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-3000", System.currentTimeMillis()+3000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-4000", System.currentTimeMillis()+4000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-5000", System.currentTimeMillis()+5000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-6000", System.currentTimeMillis()+6000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-7000", System.currentTimeMillis()+7000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-8000", System.currentTimeMillis()+8000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-9000", System.currentTimeMillis()+9000),
                    new MessageData("topic_sample02", "pk-222", "data-"+i+"-10000", System.currentTimeMillis()+10000)
            ));

            Response<String> response = brokerService.produce(produceRequest);
            if (!response.isSuccess()) {
                logger.error("response:{}", response);
            }
            if (i / 1000 == 0) {
                logger.info("send msg count:{}", i*10);
            }
        }
        long cost = (System.currentTimeMillis()-start) / 1000;
        logger.info("cost= {} s / 10W, qps = {}", cost, 10000*10/cost);

    }

    @Test
    public void consumeTest() {
        // client
        BrokerService brokerService = buildClient();

        ConsumeRequest consumeRequest = new ConsumeRequest();
        consumeRequest.setAccessToken("defaultaccesstoken");
        consumeRequest.setMessageList(Arrays.asList(
                new MessageData(15, 2, ConsumeLogUtil.generateConsumeLog("消费消息", "aaaa，IP="+ IPTool.getIp())),
                new MessageData(16, 3, ConsumeLogUtil.generateConsumeLog("消费消息", "bbbb，IP="+ IPTool.getIp())),
                new MessageData(17, 4, ConsumeLogUtil.generateConsumeLog("消费消息", "cccc，IP="+ IPTool.getIp()))
        ));

        Response<String> response = brokerService.consume(consumeRequest);
        logger.info("response:{}", response);
    }

}
