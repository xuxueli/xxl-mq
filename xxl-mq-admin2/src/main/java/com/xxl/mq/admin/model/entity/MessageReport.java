package com.xxl.mq.admin.model.entity;

import java.util.Date;

public class MessageReport {

    private int id;
    private Date produceDay;
    private long runningCount;
    private long sucCount;
    private long failCount;
    private Date updateTime;

    // other
    private long totalCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getProduceDay() {
        return produceDay;
    }

    public void setProduceDay(Date produceDay) {
        this.produceDay = produceDay;
    }

    public long getRunningCount() {
        return runningCount;
    }

    public void setRunningCount(long runningCount) {
        this.runningCount = runningCount;
    }

    public long getSucCount() {
        return sucCount;
    }

    public void setSucCount(long sucCount) {
        this.sucCount = sucCount;
    }

    public long getFailCount() {
        return failCount;
    }

    public void setFailCount(long failCount) {
        this.failCount = failCount;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public long getTotalCount() {
        //return totalCount;
        return runningCount + sucCount + failCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

}
