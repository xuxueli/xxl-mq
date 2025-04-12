package com.xxl.mq.admin.model.entity;

import java.io.Serializable;
import java.util.Date;

/**
*  Instance Entity
*
*  Created by xuxueli on '2025-03-16 12:28:40'.
*/
public class Instance implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
    * id
    */
    private long id;

    /**
    * AppName（服务唯一标识）
    */
    private String appname;

    /**
    * 节点唯一标识
    */
    private String uuid;

    /**
    * 节点最后心跳时间，动态注册时判定是否过期
    */
    private Date registerHeartbeat;

    /**
     * 注册数据
     */
    private String registryData;

    /**
    * 新增时间
    */
    private Date addTime;

    /**
    * 更新时间
    */
    private Date updateTime;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAppname() {
        return appname;
    }

    public void setAppname(String appname) {
        this.appname = appname;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getRegisterHeartbeat() {
        return registerHeartbeat;
    }

    public void setRegisterHeartbeat(Date registerHeartbeat) {
        this.registerHeartbeat = registerHeartbeat;
    }

    public String getRegistryData() {
        return registryData;
    }

    public void setRegistryData(String registryData) {
        this.registryData = registryData;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}