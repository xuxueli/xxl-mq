package com.xxl.mq.core.openapi;

import com.xxl.mq.core.openapi.model.MessageData;
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
     * @param instanceData
     * @return
     */
    public Response<String> registry(String registryRequest);

    /**
     * produce messages
     *
     * @param messageList
     * @return
     */
    public Response<String> produce(List<MessageData> messageList);

    /**
     * pull messages (will lock)
     *
     * @param pullRequest
     * @return
     */
    public Response<List<MessageData>> pull(String pullRequest);

    /**
     * consume messages (callback)
     *
     * @param messageList
     * @return
     */
    public Response<String> consumeRequest(List<MessageData> consumeRequest);


}
