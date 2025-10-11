package com.xxl.mq.admin.broker.router.impl;

import com.xxl.mq.admin.broker.router.PartitionRouter;
import com.xxl.mq.admin.util.PartitionUtil;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static com.xxl.mq.admin.util.PartitionUtil.MAX_PARTITION;

public class RandomPartitionRouter implements PartitionRouter {
    @Override
    public int route(String topic, long bizId, Map<String, PartitionUtil.PartitionRange> instancePartitionRange) {
        return 1 + ThreadLocalRandom.current().nextInt(MAX_PARTITION);      // range：[1, 10000]
    }
}
