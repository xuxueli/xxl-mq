package com.xxl.mq.admin.openapi.registry.thread;


import com.alibaba.fastjson2.JSON;
import com.xxl.mq.admin.constant.enums.AccessTokenStatuEnum;
import com.xxl.mq.admin.model.entity.AccessToken;
import com.xxl.mq.admin.openapi.registry.config.RegistryFactory;
import com.xxl.mq.admin.openapi.common.model.OpenApiRequest;
import com.xxl.tool.core.CollectionTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AccessToken Helpler
 *
 * 功能：
 * 1、AccessToken 本次缓存，合法性校验
 *
 * @author xuxueli
 */
public class AccessTokenHelpler {
    private static Logger logger = LoggerFactory.getLogger(AccessTokenHelpler.class);

    /**
     * AccessToken refresh Interval, by second
     */
    public static final int ACCESSTOKEN_REFRESH_TIME = 30;

    /**
     * 客户端监听器

     */
    private volatile Set<String> accessTokenStore = new ConcurrentSkipListSet<>();

    /**
     * registry monitor (will remove instance that expired more than 1 day)
     */
    private Thread accessTokenThread;

    /**
     * thread stop variable
     */
    private volatile boolean toStop = false;


    /**
     * start
     */
    public void start() {

        // deferredResultMonitorThread， for clean
        accessTokenThread = startThread(new Runnable() {
            @Override
            public void run() {
                logger.info(">>>>>>>>>>> xxl-mq, AccessTokenHelpler-accessTokenThread start.");
                while (!toStop) {
                    try {
                        // build new data
                        ConcurrentSkipListSet<String> accessTokenStoreNew = new ConcurrentSkipListSet<>();

                        // query valid accesstoken data
                        List<AccessToken> accessTokenList = RegistryFactory.getInstance().getAccessTokenMapper().queryValidityAccessToken(AccessTokenStatuEnum.NORMAL.getValue());
                        if (CollectionTool.isNotEmpty(accessTokenList)) {
                            accessTokenStoreNew.addAll(accessTokenList.stream()
                                    .map(AccessToken::getAccessToken)
                                    .collect(Collectors.toSet()));
                        }

                        // do refresh
                        String newDataJson = JSON.toJSONString(accessTokenStoreNew);
                        if (!Objects.equals(newDataJson, JSON.toJSONString(accessTokenStore))) {
                            logger.info(">>>>>>>>>>> xxl-mq, AccessTokenHelpler-accessTokenThread found diff data, accessTokenStoreNew:{}", newDataJson);
                            accessTokenStore = accessTokenStoreNew;
                        }
                    } catch (Throwable e) {
                        if (!toStop) {
                            logger.error(">>>>>>>>>>> xxl-mq, AccessTokenHelpler-accessTokenThread error:{}", e.getMessage(), e);
                        }
                    }
                    try {
                        TimeUnit.SECONDS.sleep(ACCESSTOKEN_REFRESH_TIME);
                    } catch (Throwable e) {
                        if (!toStop) {
                            logger.error(">>>>>>>>>>> xxl-mq, AccessTokenHelpler-accessTokenThread error2:{}", e.getMessage(), e);
                        }
                    }
                }
                logger.info(">>>>>>>>>>> xxl-mq, AccessTokenHelpler-accessTokenThread stop.");
            }
        }, "xxl-mq, AccessTokenHelpler-accessTokenThread");

    }

    public void stop() {
        // mark stop
        toStop = true;
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        // stop thread
        stopThread(accessTokenThread);
    }

    // ---------------------- util ----------------------

    /**
     * start thread
     *
     * @param runnable
     * @param name
     * @return
     */
    public static Thread startThread(Runnable runnable, String name) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.setName(name);
        thread.start();
        return thread;
    }

    /**
     * stop thread
     *
     * @param thread
     */
    public static void stopThread(Thread thread) {
        if (thread.getState() != Thread.State.TERMINATED){
            // interrupt and wait
            thread.interrupt();
            try {
                thread.join();
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // ---------------------- helper ----------------------


    /**
     * valid Request Token
     *
     * @param request
     * @return
     */
    public boolean validRequestToken(OpenApiRequest request) {
        return request!=null
                && request.getAccessToken()!=null
                && accessTokenStore.contains(request.getAccessToken());
    }

}
