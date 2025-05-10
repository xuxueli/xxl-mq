package com.xxl.mq.admin.constant.enums;

import com.xxl.mq.admin.broker.router.PartitionRouter;
import com.xxl.mq.admin.broker.router.impl.*;

/**
 * Partition Route Strategy
 *
 * @author xuxueli
 */
public enum PartitionRouteStrategyEnum {

    HASH(1, "Hash", new HashPartitionRouter()),
    RANDOM(2, "随机", new RandomPartitionRouter()),
    CYCLE(3, "轮询", new CyclePartitionRouter()),
    FIRST(4, "第一个", new FirstPartitionRouter()),
    LAST(5, "最后一个", new LastPartitionRouter()),
    BROADCAST(6, "广播", null);

    private int value;
    private String desc;
    private PartitionRouter partitionRouter;

    PartitionRouteStrategyEnum(int value, String desc, PartitionRouter partitionRouter) {
        this.value = value;
        this.desc = desc;
        this.partitionRouter = partitionRouter;
    }

    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }

    public PartitionRouter getPartitionRouter() {
        return partitionRouter;
    }

    public static PartitionRouteStrategyEnum match(int value, PartitionRouteStrategyEnum defaultItem){
        for (PartitionRouteStrategyEnum item: PartitionRouteStrategyEnum.values()) {
            if (item.value == value) {
                return item;
            }
        }
        return defaultItem;
    }

}
