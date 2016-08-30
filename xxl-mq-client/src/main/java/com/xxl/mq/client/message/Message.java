package com.xxl.mq.client.message;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 42L;

    public enum Status{NEW, ING, SUCCESS, FAIL, TIMEOUT;}

    private int id;
    private String name;		        // 消息主题
    private Map<String, String> data;	// 消息数据, Map<String, String>对象系列化的JSON字符串
    private Date delayTime;		        // 延迟执行的时间, new Date()立即执行, 否则在延迟时间点之后开始执行;
    private Status status;		        // 消息状态: NEW=新消息、ING=消费中、SUCCESS=消费成功、FAIL=消费失败、TIMEOUT=超时
    private String msg;			        // 历史流转日志
    private int retryCount;		// 充实次数, 默认0不重试

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }

    public Date getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(Date delayTime) {
        this.delayTime = delayTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
