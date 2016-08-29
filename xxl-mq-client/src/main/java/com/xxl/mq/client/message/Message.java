package com.xxl.mq.client.message;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 42L;

    public enum Destination{TOPIC, QUEUE, CONCURRENT_QUEUE;}
    public enum Status{NEW, ING, SUCCESS, FAIL, TIMEOUT;}

    private int id;
    private String name;		        // 消息主题
    private Destination destination;	// 消息类型：TOPIC=广播、QUEUE=串行队列、CONCURRENT_QUEUE=并发队列
    private Map<String, String> data;	// 消息数据, Map<String, String>对象系列化的JSON字符串
    private Date delayTime;		        // 延迟执行的时间, new Date()立即执行, 否则在延迟时间点之后开始执行;
    private Status status;		        // 消息状态: NEW=新消息、ING=消费中、SUCCESS=消费成功、FAIL=消费失败、TIMEOUT=超时
    private String msg;			        // 历史流转日志

    public Message() {
    }
    public Message(String name, Destination destination, Map<String, String> data, Date delayTime) {
        this.name = name;
        this.destination = destination;
        this.data = data;
        this.delayTime = (delayTime!=null)?delayTime:new Date();
        this.status = Status.NEW;
    }

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

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
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
}
