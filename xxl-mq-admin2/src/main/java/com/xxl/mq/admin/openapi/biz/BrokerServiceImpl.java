package com.xxl.mq.admin.openapi.biz;

import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.MessageData;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.response.Response;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * broker service
 *
 * Created by xuxueli on 16/8/28.
 */
@Service
public class BrokerServiceImpl implements BrokerService {

    @Override
    public Response<String> registry(String registryRequest) {
        return Response.ofFail("todo1");
    }

    @Override
    public Response<String> produce(List<MessageData> messageList) {
        return Response.ofFail("todo2");
    }

    @Override
    public Response<List<MessageData>> pull(String pullRequest) {
        return Response.ofSuccess(CollectionTool.newArrayList(new MessageData("111"), new MessageData("222")));
    }

    @Override
    public Response<String> consumeRequest(List<MessageData> consumeRequest) {
        return Response.ofFail("todo3");
    }

}
