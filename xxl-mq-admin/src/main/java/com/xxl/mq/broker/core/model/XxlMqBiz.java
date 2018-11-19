package com.xxl.mq.broker.core.model;

/**
 * @author xuxueli 2018-11-19
 */
public class XxlMqBiz {

    private int id;             // 业务线ID
    private String bizName;     // 业务线名称
    private int order;          // 顺序

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

}
