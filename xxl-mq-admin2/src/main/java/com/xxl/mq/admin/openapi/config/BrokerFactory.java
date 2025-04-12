package com.xxl.mq.admin.openapi.config;

import com.xxl.mq.admin.constant.enums.AccessTokenStatuEnum;
import com.xxl.mq.admin.mapper.*;
import com.xxl.mq.admin.model.entity.AccessToken;
import com.xxl.mq.admin.model.entity.Instance;
import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.RegistryRequest;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.concurrent.MessageQueue;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.jsonrpc.JsonRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * registry config
 *
 * @author xuxueli
 */
@Configuration
public class BrokerFactory implements InitializingBean, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(BrokerFactory.class);


    // ---------------------- instance ----------------------
    private static BrokerFactory instance;
    public static BrokerFactory getInstance() {
        return instance;
    }


    // ---------------------- start / stop ----------------------

    @Resource
    private BrokerService brokerService;
    @Resource
    private ApplicationMapper applicationMapper;
    @Resource
    private AccessTokenMapper accessTokenMapper;
    @Resource
    private InstanceMapper instanceMapper;


    @Override
    public void afterPropertiesSet() throws Exception {
        // base init
        instance = this;

        // 1、AccessTokenThread
        startAccessTokenThread();

        // 2、Registry MessageQueue
        startRegistryMessageQueue();
    }

    @Override
    public void destroy() throws Exception {
        // stop
    }


    // ---------------------- openapi JsonRpcServer ----------------------

    @Bean
    public JsonRpcServer jsonRpcServer() {
        JsonRpcServer jsonRpcServer = new JsonRpcServer();
        jsonRpcServer.register("brokerService", brokerService);

        return jsonRpcServer;
    }


    // ---------------------- AccessToken ----------------------

    /**
     * AccessToken LocalStore
     */
    private volatile Set<String> accessTokenStore = new ConcurrentSkipListSet<>();

    /**
     * start AccessTokenThread (will stop with jvm)
     */
    private void startAccessTokenThread() {
        CyclicThread accessTokenThread = new CyclicThread("accessTokenThread", true, 30 * 1000, new Runnable() {
            @Override
            public void run() {
                try {
                    // build new data
                    ConcurrentSkipListSet<String> accessTokenStoreNew = new ConcurrentSkipListSet<>();

                    // query valid accesstoken data
                    List<AccessToken> accessTokenList = accessTokenMapper.queryValidityAccessToken(AccessTokenStatuEnum.NORMAL.getValue());
                    if (CollectionTool.isNotEmpty(accessTokenList)) {
                        accessTokenStoreNew.addAll(accessTokenList.stream()
                                .map(AccessToken::getAccessToken)
                                .collect(Collectors.toSet()));
                    }

                    // do refresh
                    String newDataJson = GsonTool.toJson(accessTokenStoreNew);
                    if (!Objects.equals(newDataJson, GsonTool.toJson(accessTokenStore))) {
                        logger.info(">>>>>>>>>>> xxl-mq, accessTokenThread found diff data, accessTokenStoreNew:{}", newDataJson);
                        accessTokenStore = accessTokenStoreNew;
                    }
                } catch (Exception e) {
                    logger.error("AccessTokenHelpler error:", e);
                }
            }
        });
        accessTokenThread.start();
    }

    /**
     * valid  AccessToken
     *
     * @param accessToken
     * @return
     */
    public boolean validAccessToken(String accessToken) {
        return accessToken!=null && accessTokenStore.contains(accessToken);
    }

    // ---------------------- Registry ----------------------

    private volatile MessageQueue<RegistryRequest> registryMessageQueue;

    /**
     * start Registry MessageQueue (will stop with jvm)
     */
    private void startRegistryMessageQueue() {
        registryMessageQueue = new MessageQueue<RegistryRequest>(
                "registryMessageQueue",
                1000,
                messages -> {
                    for (RegistryRequest registryRequest : messages) {
                        // write registry
                        String registryDate = GsonTool.toJson(registryRequest);

                        Instance newInstance = new Instance();
                        newInstance.setAppname(registryRequest.getAppname());
                        newInstance.setUuid(registryRequest.getInstanceUuid());
                        newInstance.setRegisterHeartbeat(new Date());
                        newInstance.setRegistryData(registryDate);

                        instanceMapper.insert(newInstance); // todo, save or update
                    }
                },
                1,
                1);
    }

    /**
     * registry
     *
     * @param registryRequest
     * @return
     */
    public boolean registry(RegistryRequest registryRequest) {
        return registryMessageQueue.produce(registryRequest);
    }


}
