package com.xxl.mq.admin.core.model;

import java.util.Date;

/**
 * @author xuxueli 2018-11-23
 */
public class XxlCommonRegistryData {

    private int id;
    private String key;         // 注册Key
    private String value;       // 注册Value
    private Date updateTime;    // 更新时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
