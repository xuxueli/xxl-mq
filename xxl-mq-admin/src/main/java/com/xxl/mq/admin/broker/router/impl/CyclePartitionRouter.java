package com.xxl.mq.admin.broker.router.impl;

import com.xxl.mq.admin.broker.router.PartitionRouter;
import com.xxl.mq.admin.util.PartitionUtil;
import com.xxl.tool.core.MapTool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static com.xxl.mq.admin.util.PartitionUtil.MAX_PARTITION;

public class CyclePartitionRouter implements PartitionRouter {

    private static ConcurrentMap<Integer, AtomicInteger> routeCountEachTopic = new ConcurrentHashMap<>();
    private static long CACHE_VALID_TIME = 0;

    private static int calculate(int paramData) {

        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            routeCountEachTopic.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000*60*60*24;
        }

        AtomicInteger count = routeCountEachTopic.get(paramData);
        if (count == null || count.get() > 1000000) {
            // 初始化时主动Random一次，缓解首次压力
            count = new AtomicInteger(new Random().nextInt(100));
        } else {
            // count++
            count.addAndGet(1);
        }
        routeCountEachTopic.put(paramData, count);
        return count.get();
    }

    @Override
    public int route(String topic, long bizId, Map<String, PartitionUtil.PartitionRange> instancePartitionRange) {

        // valid
        if (MapTool.isEmpty(instancePartitionRange)) {
            // instance empty, Adjust to random-router
            return ThreadLocalRandom.current().nextInt(MAX_PARTITION);
        }

        // cycle id
        int topicHashCode = topic.hashCode()>0
                ?topic.hashCode()% MAX_PARTITION
                :(topic.hashCode() & Integer.MAX_VALUE) % MAX_PARTITION;
        int topicConsumerCount = calculate(topicHashCode);

        // cycle key
        List<String> keyList = new ArrayList<>(instancePartitionRange.keySet());
        String cycleKey = keyList.get(topicConsumerCount%keyList.size());

        return instancePartitionRange.get(cycleKey).getPartitionIdFrom();   // range：instance-partition [from, to]
    }
}
