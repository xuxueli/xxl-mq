package com.xxl.mq.admin.constant.enums;

/**
 * Topic Status
 *
 * @author xuxueli
 */
public enum TopicStatusEnum {

    NORMAL(0, "正常"),
    INACTIVE(1, "禁用");

    private int value;
    private String desc;

    TopicStatusEnum(int value, String desc) {
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

    public static TopicStatusEnum match(int value, TopicStatusEnum defaultItem){
        for (TopicStatusEnum item: TopicStatusEnum.values()) {
            if (item.value == value) {
                return item;
            }
        }
        return defaultItem;
    }

}
