package com.xxl.mq.admin.router;


import com.xxl.mq.admin.util.PartitionUtil;

import java.util.Map;

/**
 * partition router
 *
 * @author xuxueli
 */
public interface PartitionRouter {


    /**
     * partition router
     *
     * @param topic
     * @param partitionKey
     * @param instancePartitionRange
     * @return
     */
    public abstract int route(String topic,
                              String partitionKey,
                              Map<String, PartitionUtil.PartitionRange> instancePartitionRange);

}
