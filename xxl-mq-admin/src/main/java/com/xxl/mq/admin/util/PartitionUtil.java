package com.xxl.mq.admin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Partition Util
 */
public class PartitionUtil {
    private static Logger logger = LoggerFactory.getLogger(PartitionUtil.class);

    /**
     * max partition, The legal range is [1, 10000].
     */
    public static final int MAX_PARTITION = 10000;

    /**
     * valid PartitionId
     *
     * @param partitionId
     * @return
     */
    public boolean validPartitionId(int partitionId) {
        return partitionId >= 1 && partitionId <= MAX_PARTITION;
    }

    /**
     * calculate each instance's partitionId range
     *
     * @param instanceList
     * @return
     */
    public static TreeMap<String, PartitionRange> allocatePartition(List<String> instanceList) {
        TreeMap<String, PartitionRange> partitionRanges = new TreeMap<>();

        int instanceCount = instanceList.size();                    // 总实例数
        int basePartitions = MAX_PARTITION / instanceCount;       // 基本分区数数量
        int extraPartitions = MAX_PARTITION % instanceCount;      // 剩余分区数数量，
        int currentPartition = 1;

        for (int i = 0; i < instanceCount; i++) {
            int partitionsForThisInstance = basePartitions + (i < extraPartitions ? 1 : 0);     // 分配分区 = 基础分区数 + 额外分区（取余 判断是否需要分配）

            int fromPartition = currentPartition;
            int toPartition = currentPartition + partitionsForThisInstance - 1;

            currentPartition += partitionsForThisInstance;                                      // next instance

            partitionRanges.put(instanceList.get(i), new PartitionRange(fromPartition, toPartition));
        }

        return partitionRanges;
    }

    /**
     * Partition Range
     */
    public static class PartitionRange {
        private int partitionIdFrom;
        private int partitionIdTo;

        public PartitionRange(int partitionIdFrom, int partitionIdTo) {
            this.partitionIdFrom = partitionIdFrom;
            this.partitionIdTo = partitionIdTo;
        }

        public int getPartitionIdFrom() {
            return partitionIdFrom;
        }

        public void setPartitionIdFrom(int partitionIdFrom) {
            this.partitionIdFrom = partitionIdFrom;
        }

        public int getPartitionIdTo() {
            return partitionIdTo;
        }

        public void setPartitionIdTo(int partitionIdTo) {
            this.partitionIdTo = partitionIdTo;
        }

        @Override
        public String toString() {
            return "PartitionRange{" +
                    "partitionIdFrom=" + partitionIdFrom +
                    ", partitionIdTo=" + partitionIdTo +
                    '}';
        }
    }

}
