package com.xxl.mq.admin.core.model;

import java.util.Date;

/**
 * @author xuxueli 2018-11-23
 */
public class XxlCommonRegistryMessage {

    private int id;
    private String data;
    private Date addTime;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

}
