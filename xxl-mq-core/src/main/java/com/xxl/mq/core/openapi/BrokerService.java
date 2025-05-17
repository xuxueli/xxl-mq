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
     * @param registryRequest
     * @return
     */
    public Response<String> registry(RegistryRequest registryRequest);

    /**
     * registry remove instance
     *
     * @param registryRequest
     * @return
     */
    public Response<String> registryRemove(RegistryRequest registryRequest);

    /**
     * produce messages (new message)
     *
     * @param produceRequest
     * @return
     */
    public Response<String> produce(ProduceRequest produceRequest);

    /**
     * consume messages (callback message status)
     *
     * @param consumeRequest
     * @return
     */
    public Response<String> consume(ConsumeRequest consumeRequest);

    /**
     * pull messages (will lock message)
     *
     * @param pullRequest
     * @return
     */
    public Response<List<MessageData>> pullAndLock(PullRequest pullRequest);

    /**
     * pull pre-check (check partition permission)
     *
     * @param pullRequest
     * @return
     */
    public Response<String> pullPreCheck(PullRequest pullRequest);

}
