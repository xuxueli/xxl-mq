package com.xxl.mq.admin.model.dto;

import com.xxl.mq.admin.util.PartitionUtil;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;


/**
 * Created by xuxueli on 16/8/28.
 */
public class ApplicationRegistryData implements Serializable {
    private static final long serialVersionUID = 42L;

    private Map<String, PartitionUtil.PartitionRange> instancePartitionRange;

    private Map<String, Set<String>> topicGroup;

    public Map<String, PartitionUtil.PartitionRange> getInstancePartitionRange() {
        return instancePartitionRange;
    }

    public void setInstancePartitionRange(Map<String, PartitionUtil.PartitionRange> instancePartitionRange) {
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

}
