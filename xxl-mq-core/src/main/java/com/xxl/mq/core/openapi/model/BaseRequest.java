package com.xxl.mq.core.openapi.model;

import java.io.Serializable;

/**
 * Created by xuxueli on 16/8/28.
 */
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 42L;

    private String accesstoken;

    public String getAccesstoken() {
        return accesstoken;
    }

    public void setAccesstoken(String accesstoken) {
        this.accesstoken = accesstoken;
    }

}
