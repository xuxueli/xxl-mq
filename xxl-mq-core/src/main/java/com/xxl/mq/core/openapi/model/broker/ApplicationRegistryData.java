package com.xxl.mq.core.openapi.model.broker;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;


/**
 * Created by xuxueli on 16/8/28.
 */
public class ApplicationRegistryData implements Serializable {
    private static final long serialVersionUID = 42L;

    private Map<String, PartitionRange> instancePartitionRange;

    private Map<String, Set<String>> topicGroup;

    public Map<String, PartitionRange> getInstancePartitionRange() {
        return instancePartitionRange;
    }

    public void setInstancePartitionRange(Map<String, PartitionRange> instancePartitionRange) {
        this.instancePartitionRange = instancePartitionRange;
    }

    public Map<String, Set<String>> getTopicGroup() {
        return topicGroup;
    }

    public void setTopicGroup(Map<String, Set<String>> topicGroup) {
        this.topicGroup = topicGroup;
    }

    @Override
    public String toString() {
        return "ApplicationRegistryData{" +
                "instancePartitionRange=" + instancePartitionRange +
                ", topicGroup=" + topicGroup +
                '}';
    }

    // other

    public static class PartitionRange implements Serializable {
        private int partitionFrom;
        private int partitionTo;

        public int getPartitionFrom() {
            return partitionFrom;
        }

        public void setPartitionFrom(int partitionFrom) {
            this.partitionFrom = partitionFrom;
        }

        public int getPartitionTo() {
            return partitionTo;
        }

        public void setPartitionTo(int partitionTo) {
            this.partitionTo = partitionTo;
        }

        @Override
        public String toString() {
            return "PartitionRange{" +
                    "partitionFrom=" + partitionFrom +
                    ", partitionTo=" + partitionTo +
                    '}';
        }
    }

}
