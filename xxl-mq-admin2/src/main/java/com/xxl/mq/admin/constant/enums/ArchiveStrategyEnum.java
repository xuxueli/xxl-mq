package com.xxl.mq.admin.constant.enums;

/**
 * Archive Strategy
 * @author xuxueli
 */
public enum ArchiveStrategyEnum {

    RESERVE_7_DAY(1, "归档保留7天"),
    RESERVE_30_DAY(2, "归档保留30天"),
    RESERVE_90_DAY(3, "归档保留90天"),
    RESERVE_FOREVER(9, "归档永久保留"),
    NONE(10, "不归档直接清理");

    private int value;
    private String desc;

    ArchiveStrategyEnum(int value, String desc) {
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

    public static ArchiveStrategyEnum match(int value, ArchiveStrategyEnum defaultItem){
        for (ArchiveStrategyEnum item: ArchiveStrategyEnum.values()) {
            if (item.value == value) {
                return item;
            }
        }
        return defaultItem;
    }

}
