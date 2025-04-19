package com.xxl.mq.admin.model.dto;

import com.xxl.mq.admin.util.PartitionUtil;

import java.io.Serializable;
import java.util.TreeMap;


/**
 * Created by xuxueli on 16/8/28.
 */
public class ApplicationRegistryData implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * instancePartitionRange
     *      key：Instance#uuid
     *      Value：PartitionRange
     */
    private TreeMap<String, PartitionUtil.PartitionRange> instancePartitionRange;

    public TreeMap<String, PartitionUtil.PartitionRange> getInstancePartitionRange() {
        return instancePartitionRange;
    }

    public void setInstancePartitionRange(TreeMap<String, PartitionUtil.PartitionRange> instancePartitionRange) {
        this.instancePartitionRange = instancePartitionRange;
    }

    @Override
    public String toString() {
        return "ApplicationRegistryData{" +
                "instancePartitionRange=" + instancePartitionRange +
                '}';
    }

}
