package com.xxl.mq.admin.broker.router.impl;

import com.xxl.mq.admin.broker.router.PartitionRouter;
import com.xxl.mq.admin.util.PartitionUtil;

import java.util.Map;

public class LastPartitionRouter implements PartitionRouter {
    @Override
    public int route(String topic, long bizId, Map<String, PartitionUtil.PartitionRange> instancePartitionRange) {
        return 10000;
    }
}
