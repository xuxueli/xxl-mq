package com.xxl.mq.core.openapi.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;


/**
 * Created by xuxueli on 16/8/28.
 */
public class RegistryRequest extends BaseRequest implements Serializable {
    private static final long serialVersionUID = 42L;

    private String appname;
    private String instanceUuid;
    private Map<String, Set<String>> topicGroup;

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

    public Map<String, Set<String>> getTopicGroup() {
        return topicGroup;
    }

    public void setTopicGroup(Map<String, Set<String>> topicGroup) {
        this.topicGroup = topicGroup;
    }

    @Override
    public String toString() {
        return "RegistryRequest{" +
                "appname='" + appname + '\'' +
                ", instanceUuid='" + instanceUuid + '\'' +
                ", topicGroup=" + topicGroup +
                '}';
    }

}
