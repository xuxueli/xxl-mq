package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerFactory;
import com.xxl.mq.admin.constant.enums.AccessTokenStatuEnum;
import com.xxl.mq.admin.model.entity.AccessToken;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.gson.GsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * AccessTokenThreadHelper
 *
 * @author xuxueli
 */
public class AccessTokenThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger(AccessTokenThreadHelper.class);

    // ---------------------- init ----------------------

    private final BrokerFactory brokerFactory;
    public AccessTokenThreadHelper(BrokerFactory brokerFactory) {
        this.brokerFactory = brokerFactory;
    }

    // ---------------------- start / stop ----------------------

    /**
     * AccessToken LocalStore
     */
    private volatile Set<String> accessTokenStore = new ConcurrentSkipListSet<>();

    /**
     * beat time interval, 30s
     */
    public static final int BEAT_TIME_INTERVAL = 30 * 1000;

    /**
     * start AccessTokenThread (will stop with jvm)
     */
    public void start(){
        CyclicThread accessTokenThread = new CyclicThread("accessTokenThread", true, new Runnable() {
            @Override
            public void run() {
                try {
                    // build new data
                    ConcurrentSkipListSet<String> accessTokenStoreNew = new ConcurrentSkipListSet<>();

                    // query valid accesstoken data
                    List<AccessToken> accessTokenList = brokerFactory.getAccessTokenMapper().queryValidityAccessToken(AccessTokenStatuEnum.NORMAL.getValue());
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
        }, BEAT_TIME_INTERVAL, true);
        accessTokenThread.start();
    }

    public void stop(){
        // do nothing
    }

    // ---------------------- tool ----------------------

    /**
     * valid  AccessToken
     *
     * @param accessToken
     * @return
     */
    public boolean validAccessToken(String accessToken) {
        return accessToken!=null && accessTokenStore.contains(accessToken);
    }

}
