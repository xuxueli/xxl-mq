package com.xxl.mq.admin.business.broker.router.impl;

import com.xxl.mq.admin.business.broker.router.PartitionRouter;
import com.xxl.mq.admin.business.util.PartitionUtil;

import java.util.Map;

public class LastPartitionRouter implements PartitionRouter {
    @Override
    public int route(String topic, long bizId, Map<String, PartitionUtil.PartitionRange> instancePartitionRange) {
        return PartitionUtil.MAX_PARTITION;
    }
}
