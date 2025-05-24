package com.xxl.mq.admin.model.dto;

import java.io.Serializable;
import java.util.Date;

/**
*  Message Entity
*
*  Created by xuxueli on '2025-03-21 21:54:06'.
*/
public class MessageDTO implements Serializable {
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
    * 消息分片ID
    */
    private int partitionId;

    /**
     * 消息数据
     */
    private String data;

    /**
     * 消息关联的 业务ID（用于结合“分区路由”生成 partitionId）
     */
    private long bizId;

    /**
    * 状态
    */
    private int status;

    /**
    * 生效时间
    */
    private String effectTime;

    /**
     * 剩余重试次数
     */
    private int retryCountRemain;

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
    private String addTime;

    /**
    * 更新时间
    */
    private String updateTime;


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

    public long getBizId() {
        return bizId;
    }

    public void setBizId(long bizId) {
        this.bizId = bizId;
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

    public int getRetryCountRemain() {
        return retryCountRemain;
    }

    public void setRetryCountRemain(int retryCountRemain) {
        this.retryCountRemain = retryCountRemain;
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

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}