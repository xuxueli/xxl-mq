package com.xxl.mq.core.openapi.model;

import java.io.Serializable;

/**
 * message data
 *
 * Created by xuxueli on 2025
 */
public class MessageData implements Serializable {
    private static final long serialVersionUID = 42L;

    public MessageData() {
    }
    // for produce
    public MessageData(String topic, String data, long bizId, long effectTime) {
        this.topic = topic;
        this.data = data;
        this.bizId = bizId;
        this.effectTime = effectTime;
    }
    // for pull
    public MessageData(long id, String topic, String data, long effectTime, Integer executionTimeout) {
        this.id = id;
        this.topic = topic;
        this.data = data;
        this.effectTime = effectTime;
        this.executionTimeout = executionTimeout;
    }
    // for consume
    public MessageData(long id, String topic, int status, String consumeLog, String consumeInstanceUuid) {
        this.id = id;
        this.topic = topic;
        this.status = status;
        this.consumeLog = consumeLog;
        this.consumeInstanceUuid = consumeInstanceUuid;
    }

    /**
     * id
     */
    private long id;

    /**
     * 消息主题Topic
     */
    private String topic;

    /**
     * 消息数据
     */
    private String data;

    /**
     * 消息关联的 业务ID（用于结合“分区路由”生成 partitionId）
     */
    /*private int partitionId;*/
    private long bizId;

    /**
     * 生效时间（时间戳；为空使用当前时间）
     */
    private long effectTime;

    /**
     * 状态
     *      NEW(0, "未消费"),
     *      RUNNING(1, "消费中"),
     *      EXECUTE_SUCCESS(2, "消费成功"),
     *      EXECUTE_FAIL(3, "消费失败"),
     *      EXECUTE_TIMEOUT(4, "消费超时");
     */
    private int status;

    /**
     * 消费日志
     */
    private String consumeLog;

    /**
     * 消费实例实例唯一标识
     */
    private String consumeInstanceUuid;

    // plugin : topic execution timeout
    private Integer executionTimeout;

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

    public long getEffectTime() {
        return effectTime;
    }

    public void setEffectTime(long effectTime) {
        this.effectTime = effectTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public Integer getExecutionTimeout() {
        return executionTimeout;
    }

    public void setExecutionTimeout(Integer executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    @Override
    public String toString() {
        return "MessageData{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", data='" + data + '\'' +
                ", bizId=" + bizId +
                ", effectTime=" + effectTime +
                ", status=" + status +
                ", consumeLog='" + consumeLog + '\'' +
                ", consumeInstanceUuid='" + consumeInstanceUuid + '\'' +
                ", executionTimeout=" + executionTimeout +
                '}';
    }

}
