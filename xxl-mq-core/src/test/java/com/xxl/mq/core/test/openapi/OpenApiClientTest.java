package com.xxl.mq.core.test.openapi;

import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.*;
import com.xxl.mq.core.util.ConsumeLogUtil;
import com.xxl.tool.http.IPTool;
import com.xxl.tool.jsonrpc.JsonRpcClient;
import com.xxl.tool.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

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
            registryRequest.setAccesstoken("defaultaccesstoken");
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
            registryRequest.setAccesstoken("defaultaccesstoken");
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
            registryRequest.setAccesstoken("defaultaccesstoken");
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
        registryRequest.setAccesstoken("defaultaccesstoken");
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
        produceRequest.setAccesstoken("defaultaccesstoken");
        produceRequest.setMessageList(Arrays.asList(
                new MessageData("topic_sample", "data111", System.currentTimeMillis(), 0),
                new MessageData("topic_sample", "data222", System.currentTimeMillis()+1000, 0),
                new MessageData("topic_sample", "data333", System.currentTimeMillis()+2000, 0)
        ));

        Response<String> response = brokerService.produce(produceRequest);
        logger.info("response:{}", response);
    }

    @Test
    public void produce2Test() {
        // client
        BrokerService brokerService = buildClient();

        ProduceRequest produceRequest = new ProduceRequest();
        produceRequest.setAccesstoken("defaultaccesstoken");
        produceRequest.setMessageList(Arrays.asList(
                new MessageData("topic_sample02", "data333", System.currentTimeMillis()+2000, 0)
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
            produceRequest.setAccesstoken("defaultaccesstoken");
            produceRequest.setMessageList(Arrays.asList(
                    new MessageData("topic_sample02", "data-"+i+"-1000", System.currentTimeMillis()+1000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-2000", System.currentTimeMillis()+2000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-3000", System.currentTimeMillis()+3000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-4000", System.currentTimeMillis()+4000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-5000", System.currentTimeMillis()+5000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-6000", System.currentTimeMillis()+6000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-7000", System.currentTimeMillis()+7000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-8000", System.currentTimeMillis()+8000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-9000", System.currentTimeMillis()+9000, 0),
                    new MessageData("topic_sample02", "data-"+i+"-10000", System.currentTimeMillis()+10000, 0)
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
        consumeRequest.setAccesstoken("defaultaccesstoken");
        consumeRequest.setMessageList(Arrays.asList(
                new MessageData(15, "topic_sample", 2, "consume log", "uuid"),
                new MessageData(16, "topic_sample", 3, "consume log", "uuid"),
                new MessageData(17, "topic_sample", 4, "consume log", "uuid")
        ));

        Response<String> response = brokerService.consume(consumeRequest);
        logger.info("response:{}", response);
    }

    @Test
    public void pullTest() {
        // client
        BrokerService brokerService = buildClient();

        PullRequest pullRequest = new PullRequest();
        pullRequest.setAccesstoken("defaultaccesstoken");
        pullRequest.setAppname("xxl-mq-sample");
        pullRequest.setInstanceUuid("uuid_03");
        pullRequest.setTopicList(Arrays.asList("topic_sample"));

        Response<List<MessageData>> response = brokerService.pullAndLock(pullRequest);
        logger.info("response:{}", response);
    }

}
