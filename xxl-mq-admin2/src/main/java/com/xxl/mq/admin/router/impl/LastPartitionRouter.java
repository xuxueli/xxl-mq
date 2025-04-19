package com.xxl.mq.admin.router.impl;

import com.xxl.mq.admin.router.PartitionRouter;
import com.xxl.mq.admin.util.PartitionUtil;

import java.util.Map;

public class LastPartitionRouter implements PartitionRouter {
    @Override
    public int route(String topic, String partitionKey, Map<String, PartitionUtil.PartitionRange> instancePartitionRange) {
        return 10000;
    }
}
