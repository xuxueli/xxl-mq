package com.xxl.mq.admin.constant.enums;

/**
 * Partition Route Strategy
 *
 * @author xuxueli
 */
public enum PartitionRouteStrategyEnum {

    HASH(1, "Hash分区路由"),
    RANDOM(2, "随机分区路由"),
    CYCLE(3, "轮询分区路由");

    private int value;
    private String desc;

    PartitionRouteStrategyEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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
