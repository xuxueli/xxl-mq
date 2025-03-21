package com.xxl.mq.admin.constant.enums;

/**
 * Topic Level Strategy
 *
 * @author xuxueli
 */
public enum TopicLevelStrategyEnum {

    LEVEL_1(1, "1级"),
    LEVEL_2(2, "2级"),
    LEVEL_3(3, "3级"),
    LEVEL_4(4, "4级"),
    LEVEL_5(5, "5级");

    private int value;
    private String desc;

    TopicLevelStrategyEnum(int value, String desc) {
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

    public static TopicLevelStrategyEnum match(int value, TopicLevelStrategyEnum defaultItem){
        for (TopicLevelStrategyEnum item: TopicLevelStrategyEnum.values()) {
            if (item.value == value) {
                return item;
            }
        }
        return defaultItem;
    }

}
