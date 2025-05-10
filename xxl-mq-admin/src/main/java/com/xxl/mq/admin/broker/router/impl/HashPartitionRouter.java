package com.xxl.mq.admin.broker.router.impl;

import com.xxl.mq.admin.broker.router.PartitionRouter;
import com.xxl.mq.admin.util.PartitionUtil;

import java.util.Map;

import static com.xxl.mq.admin.util.PartitionUtil.MAX_PARTITION;

public class HashPartitionRouter implements PartitionRouter {
    @Override
    public int route(String topic, String partitionKey, Map<String, PartitionUtil.PartitionRange> instancePartitionRange) {
        String hashKey = partitionKey!=null ?partitionKey : topic;
        return hashKey.hashCode()>0
                ?hashKey.hashCode()% MAX_PARTITION
                :(hashKey.hashCode() & Integer.MAX_VALUE) % MAX_PARTITION;
    }
}
