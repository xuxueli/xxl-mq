package com.xxl.mq.admin.constant.enums;

/**
 * Retry Strategy
 * @author xuxueli
 */
public enum RetryStrategyEnum {

    FIXED_RETREAT(1, "固定间隔重试"),
    LINEAR_RETREAT(2, "线性退避重试"),
    EXPONENTIAL_RETREAT(3, "指数退避重试");

    private int value;
    private String desc;

    RetryStrategyEnum(int value, String desc) {
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

    public static RetryStrategyEnum match(int value, RetryStrategyEnum defaultItem){
        for (RetryStrategyEnum item: RetryStrategyEnum.values()) {
            if (item.value == value) {
                return item;
            }
        }
        return defaultItem;
    }

}
