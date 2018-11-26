package com.xxl.mq.admin.core.model;

import java.util.List;

/**
 * @author xuxueli 2018-11-23
 */
public class XxlCommonRegistry {

    private int id;
    private String key;         // 注册Key
    private String data;        // 注册Value有效数据

    // plugin
    private List<String> dataList;

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
    }

}
