package com.xxl.mq.core.openapi.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xuxueli on 16/8/28.
 */
public class RegistryRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 42L;

    private String appname;
    private String instanceUuid;
    private List<String> topicList;

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getInstanceUuid() {
        return instanceUuid;
    }

    public void setInstanceUuid(String instanceUuid) {
        this.instanceUuid = instanceUuid;
    }

    public List<String> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<String> topicList) {
        this.topicList = topicList;
    }

    @Override
    public String toString() {
        return "RegistryRequest{" +
                "appname='" + appname + '\'' +
                ", instanceUuid='" + instanceUuid + '\'' +
                ", topicList=" + topicList +
                '}';
    }
}
