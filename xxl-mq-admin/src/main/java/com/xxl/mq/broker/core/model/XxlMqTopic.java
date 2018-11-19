package com.xxl.mq.broker.core.model;

/**
 * @author xuxueli 2018-11-19
 */
public class XxlMqTopic {

    private String topic;           // 消息主题
    private int bizId;              // 业务线ID
    private String messageInfo;     // 消息数据信息，各状态消息量、成功率等；60s刷新一次；
    private String author;          // 负责人
    private String alarmEmails;     // 告警邮箱，多个逗号分隔；窗口期5min，只检验窗口期数据，存在消息阻塞（公共阻塞阈值），失败消息（公共失败率阈值）时，5min推送告警一次；

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getBizId() {
        return bizId;
    }

    public void setBizId(int bizId) {
        this.bizId = bizId;
    }

    public String getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(String messageInfo) {
        this.messageInfo = messageInfo;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAlarmEmails() {
        return alarmEmails;
    }

    public void setAlarmEmails(String alarmEmails) {
        this.alarmEmails = alarmEmails;
    }
}
