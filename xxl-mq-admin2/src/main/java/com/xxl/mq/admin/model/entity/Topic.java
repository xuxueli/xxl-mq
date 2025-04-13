package com.xxl.mq.admin.model.entity;

import java.io.Serializable;
import java.util.Date;

/**
*  Topic Entity
*
*  Created by xuxueli on '2025-03-21 12:52:25'.
*/
public class Topic implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
    * id
    */
    private long id;

    /**
     * appname
     */
    private String appname;

    /**
    * 消息主题Topic
    */
    private String topic;

    /**
    * 消息主题名称
    */
    private String desc;

    /**
    * 负责人
    */
    private String owner;

    /**
    * 告警配置（邮箱）
    */
    private String alarmEmail;

    /**
    * 状态：TopicStatusEnum
    */
    private int status;

    /**
    * 存储策略
    */
    private int storeStrategy;

    /**
     * 归档策略
     */
    private int archiveStrategy;

    /**
    * 分区策略
    */
    private int partitionStrategy;

    /**
    * 优先级
    */
    private int level;

    /**
    * 重试策略
    */
    private int retryStrategy;

    /**
    * 重试次数
    */
    private int retryCount;

    /**
    * 重试间隔，单位秒（3s；3/6/9；3/9/27）
    */
    private int retryInterval;

    /**
    * 执行超时时间
    */
    private int executionTimeout;

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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAlarmEmail() {
        return alarmEmail;
    }

    public void setAlarmEmail(String alarmEmail) {
        this.alarmEmail = alarmEmail;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStoreStrategy() {
        return storeStrategy;
    }

    public void setStoreStrategy(int storeStrategy) {
        this.storeStrategy = storeStrategy;
    }

    public int getArchiveStrategy() {
        return archiveStrategy;
    }

    public void setArchiveStrategy(int archiveStrategy) {
        this.archiveStrategy = archiveStrategy;
    }

    public int getPartitionStrategy() {
        return partitionStrategy;
    }

    public void setPartitionStrategy(int partitionStrategy) {
        this.partitionStrategy = partitionStrategy;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getRetryStrategy() {
        return retryStrategy;
    }

    public void setRetryStrategy(int retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getExecutionTimeout() {
        return executionTimeout;
    }

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
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