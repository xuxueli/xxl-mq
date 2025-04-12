package com.xxl.mq.core.openapi;

import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.tool.response.Response;

import java.util.List;

/**
 * broker biz
 *
 * Created by xuxueli on 16/8/28.
 */
public interface BrokerService {

    /**
     * registry instance
     *
     * @param messageList
     * @return
     */
    public Response<String> registryInstance(List<MessageData> messageList);

    /**
     * produce messages
     *
     * @param messageList
     * @return
     */
    public Response<String> produceMessages(List<MessageData> messageList);

    /**
     * pull messages (will lock)
     *
     * @param messageList
     * @return
     */
    public Response<List<MessageData>> pullMessages(List<MessageData> messageList);

    /**
     * consume messages (callback)
     *
     * @param messageList
     * @return
     */
    public Response<String> consumeMessages(List<MessageData> messageList);


}
