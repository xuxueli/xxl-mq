package com.xxl.mq.core.openapi.model;

import java.io.Serializable;

/**
 * Created by xuxueli on 16/8/28.
 */
public class BaseRequest implements Serializable {
    private static final long serialVersionUID = 42L;

    private String accessToken;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

}
