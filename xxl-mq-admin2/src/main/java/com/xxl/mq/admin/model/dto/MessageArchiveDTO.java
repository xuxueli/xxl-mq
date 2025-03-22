package com.xxl.mq.admin.model.dto;

import java.io.Serializable;
import java.util.Date;

/**
*  Message Entity
*
*  Created by xuxueli on '2025-03-21 21:54:06'.
*/
public class MessageArchiveDTO implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
    * id
    */
    private long id;

    /**
    * 消息主题Topic
    */
    private String topic;

    /**
    * 消息主题分组
    */
    private String group;

    /**
    * 消息分片ID
    */
    private int partitionId;

    /**
     * 消息数据
     */
    private String data;

    /**
    * 状态
    */
    private int status;

    /**
    * 生效时间
    */
    private String effectTime;

    /**
    * 消费日志
    */
    private String consumeLog;

    /**
    * 消费实例实例唯一标识
    */
    private String consumeInstanceUuid;

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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(int partitionId) {
        this.partitionId = partitionId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(String effectTime) {
        this.effectTime = effectTime;
    }

    public String getConsumeLog() {
        return consumeLog;
    }

    public void setConsumeLog(String consumeLog) {
        this.consumeLog = consumeLog;
    }

    public String getConsumeInstanceUuid() {
        return consumeInstanceUuid;
    }

    public void setConsumeInstanceUuid(String consumeInstanceUuid) {
        this.consumeInstanceUuid = consumeInstanceUuid;
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