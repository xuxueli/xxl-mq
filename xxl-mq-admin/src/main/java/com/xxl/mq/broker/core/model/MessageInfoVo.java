package com.xxl.mq.broker.core.model;

/**
 * @author xuxueli 2018-11-20
 */
public class MessageInfoVo {

    private long totalNum;
    private long newNum;
    private long ingNum;
    private long successNum;
    private long failNum;

    public long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(long totalNum) {
        this.totalNum = totalNum;
    }

    public long getNewNum() {
        return newNum;
    }

    public void setNewNum(long newNum) {
        this.newNum = newNum;
    }

    public long getIngNum() {
        return ingNum;
    }

    public void setIngNum(long ingNum) {
        this.ingNum = ingNum;
    }

    public long getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(long successNum) {
        this.successNum = successNum;
    }

    public long getFailNum() {
        return failNum;
    }

    public void setFailNum(long failNum) {
        this.failNum = failNum;
    }

}
