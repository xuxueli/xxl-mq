package com.xxl.mq.admin.constant.enums;

/**
 * Archive Strategy
 * @author xuxueli
 */
public enum ArchiveStrategyEnum {

    RESERVE_7_DAY(1, "归档7天内消息(清理历史消息)"),                 // 1、正式表：终止态消息（成功or失败）清理；2、归档表：写入 T-7 之后数据；历史消息清理；
    RESERVE_30_DAY(2, "归档30天内消息(清理历史消息)"),               // 1、正式表：终止态消息（成功or失败）清理；2、归档表：写入 T-30 之后数据；历史消息清理；
    RESERVE_90_DAY(3, "归档90天内消息(清理历史消息)"),               // 1、正式表：终止态消息（成功or失败）清理；2、归档表：写入 T-90 之后数据；历史消息清理；
    RESERVE_FOREVER(9, "归档全部消息(永久保留)"),                   // 1、正式表：终止态消息（成功or失败）清理；2、归档表：全部写入；永久保留；
    NONE(10, "不归档直接清理");                                    // 1、正式表：终止态消息（成功or失败）清理；2、归档表：忽略，不写入；

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
