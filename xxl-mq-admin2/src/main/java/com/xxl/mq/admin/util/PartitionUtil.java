package com.xxl.mq.admin.util;

import java.util.*;

/**
 * Partition Util
 */
public class PartitionUtil {

    private static int PARTITION_COUNT = 10000;

    /**
     * valid PartitionId
     *
     * @param partitionId
     * @return
     */
    public boolean validPartitionId(int partitionId) {
        return partitionId>=1 && partitionId<=PARTITION_COUNT;
    }

    /**
     * 计算每个 instance 分配的 partitionId 范围
     *
     * @param instanceList
     * @return
     */
    public static Map<String, PartitionRange> allocatePartition(List<String> instanceList) {
        Map<String, PartitionRange> partitionRanges = new HashMap<>();

        int instanceCount = instanceList.size();                    // 总实例数
        int basePartitions = PARTITION_COUNT / instanceCount;       // 基本分区数数量
        int extraPartitions = PARTITION_COUNT % instanceCount;      // 剩余分区数数量，
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
