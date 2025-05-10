package com.xxl.mq.admin.constant.enums;

/**
 * Store Strategy
 * @author xuxueli
 */
public enum StoreStrategyEnum {

    UNITY_STORE(0, "统一存储")/*,
    SEPARATE_STORE(1, "隔离存储")*/;

    private int value;
    private String desc;

    StoreStrategyEnum(int value, String desc) {
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

    public static StoreStrategyEnum match(int value, StoreStrategyEnum defaultItem){
        for (StoreStrategyEnum item: StoreStrategyEnum.values()) {
            if (item.value == value) {
                return item;
            }
        }
        return defaultItem;
    }

}
