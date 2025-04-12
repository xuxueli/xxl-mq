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
     * produce messages
     *
     * @param produceRequest
     * @return
     */
    public Response<String> produce(ProduceRequest produceRequest);

    /**
     * pull messages (will lock)
     *
     * @param pullRequest
     * @return
     */
    public Response<List<MessageData>> pull(PullRequest pullRequest);

    /**
     * consume messages (callback)
     *
     * @param consumeRequest
     * @return
     */
    public Response<String> consumeRequest(ConsumeRequest consumeRequest);


}
