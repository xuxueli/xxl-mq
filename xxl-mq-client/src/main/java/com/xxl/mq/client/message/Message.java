package com.xxl.mq.client.message;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    public String name;
    public Map<String, String> dataMap;

    public Message(String name, Map<String, String> dataMap) {
        this.name = name;
        this.dataMap = dataMap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }
}
