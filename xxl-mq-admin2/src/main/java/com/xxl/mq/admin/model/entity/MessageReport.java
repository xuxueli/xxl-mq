package com.xxl.mq.admin.model.entity;

import java.util.Date;

public class MessageReport {

    private Date effectTime;
    private long totalCount;
    private long successTotal;
    private long failTotal;
    private long runningTotal;

    public Date getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(Date effectTime) {
        this.effectTime = effectTime;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getSuccessTotal() {
        return successTotal;
    }

    public void setSuccessTotal(long successTotal) {
        this.successTotal = successTotal;
    }

    public long getFailTotal() {
        return failTotal;
    }

    public void setFailTotal(long failTotal) {
        this.failTotal = failTotal;
    }

    public long getRunningTotal() {
        // return runningTotal;
        // calculte
        return totalCount - successTotal - failTotal;
    }

    public void setRunningTotal(long runningTotal) {
        this.runningTotal = runningTotal;
    }

}
