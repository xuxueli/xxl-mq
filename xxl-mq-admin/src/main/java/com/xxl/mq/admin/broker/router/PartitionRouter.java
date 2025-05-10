package com.xxl.mq.admin.broker.router;


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
     * @param bizId
     * @param instancePartitionRange
     * @return
     */
    public abstract int route(String topic,
                              long bizId,
                              Map<String, PartitionUtil.PartitionRange> instancePartitionRange);

}
