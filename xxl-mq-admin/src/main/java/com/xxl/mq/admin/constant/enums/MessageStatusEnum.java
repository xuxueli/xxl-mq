package com.xxl.mq.admin.constant.enums;

/**
 * Topic Status
 *
 * @author xuxueli
 */
public enum MessageStatusEnum {

    NEW(0, "未消费"),
    RUNNING(1, "消费中"),
    EXECUTE_SUCCESS(2, "消费成功"),
    EXECUTE_FAIL(3, "消费失败"),
    EXECUTE_TIMEOUT(4, "消费超时失败");

    private int value;
    private String desc;

    MessageStatusEnum(int value, String desc) {
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

    public static MessageStatusEnum match(int value, MessageStatusEnum defaultItem){
        for (MessageStatusEnum item: MessageStatusEnum.values()) {
            if (item.value == value) {
                return item;
            }
        }
        return defaultItem;
    }

}
