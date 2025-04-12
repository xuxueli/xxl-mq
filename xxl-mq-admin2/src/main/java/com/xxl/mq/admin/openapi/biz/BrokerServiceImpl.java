package com.xxl.mq.admin.openapi.biz;

import com.xxl.mq.core.openapi.BrokerService;
import com.xxl.mq.core.openapi.model.*;
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
    public Response<String> registry(RegistryRequest registryRequest) {
        return null;
    }

    @Override
    public Response<String> produce(ProduceRequest produceRequest) {
        return null;
    }

    @Override
    public Response<List<MessageData>> pull(PullRequest pullRequest) {
        return null;
    }

    @Override
    public Response<String> consumeRequest(ConsumeRequest consumeRequest) {
        return null;
    }
}
