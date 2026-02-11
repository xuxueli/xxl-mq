package com.xxl.mq.core.openapi;

import com.xxl.mq.core.openapi.model.*;
import com.xxl.tool.response.Response;

import java.util.List;

/**
 * broker service
 *
 * Created by xuxueli on 16/8/28.
 */
public interface BrokerService {

    /**
     * registry instance
     *
     *  1、refresh instance （only appname）
     *  2、init-topic
     *  3、init-appname
     *
     * @param registryRequest
     * @return
     */
    public Response<String> registry(RegistryRequest registryRequest);

    /**
     * registry remove instance
     *
     *  1、remove instance （only appname）
     *
     * @param registryRequest
     * @return
     */
    public Response<String> registryRemove(RegistryRequest registryRequest);

    /**
     * produce messages (new message)
     *
     *  1、message (with topic)
     *
     * @param produceRequest
     * @return
     */
    public Response<String> produce(ProduceRequest produceRequest);

    /**
     * consume messages (callback message status)
     *
     *  1、message (with msg-id)
     *
     * @param consumeRequest
     * @return
     */
    public Response<String> consume(ConsumeRequest consumeRequest);

    /**
     * pull messages (will lock message)
     *
     *  1、pullAndLock message (with topic + appname)
     *
     * @param pullRequest
     * @return
     */
    public Response<List<MessageData>> pullAndLock(PullRequest pullRequest);

    /**
     * pull pre-check (check partition permission)
     *
     *  1、pullPreCheck message (with topic + appname)
     *
     * @param pullRequest
     * @return
     */
    public Response<String> pullPreCheck(PullRequest pullRequest);

}
