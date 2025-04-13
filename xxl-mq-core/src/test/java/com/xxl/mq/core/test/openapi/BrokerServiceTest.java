package com.xxl.mq.core.test.openapi;

import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.RegistryRequest;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.jsonrpc.JsonRpcClient;
import com.xxl.tool.response.Response;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

public class BrokerServiceTest {
    private static Logger logger = LoggerFactory.getLogger(BrokerServiceTest.class);

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

        // param
        RegistryRequest registryRequest = new RegistryRequest();
        registryRequest.setAccessToken("defaultaccesstoken");
        registryRequest.setAppname("xxl-mq-sample");
        registryRequest.setInstanceUuid("qwerqweqrwer");
        registryRequest.setTopicGroup(MapTool.newHashMap(
                "topic01", new HashSet<>(Arrays.asList("default")),
                "topic03", new HashSet<>(Arrays.asList("default"))
        ));

        // invoke
        Response<String> response = brokerService.registry(registryRequest);
        logger.info("response:{}", response);
    }

}
