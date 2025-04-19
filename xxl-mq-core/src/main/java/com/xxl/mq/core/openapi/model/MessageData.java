package com.xxl.mq.core.openapi.model;

import java.io.Serializable;

/**
 * message data
 *
 * Created by xuxueli on 16/8/28.
 */
public class MessageData implements Serializable {
    private static final long serialVersionUID = 42L;

    public MessageData() {
    }
    public MessageData(String topic, String partitionKey, String data, long effectTime) {
        this.topic = topic;
        this.partitionKey = partitionKey;
        this.data = data;
        this.effectTime = effectTime;
    }
    public MessageData(long id, int status, String consumeLog) {
        this.id = id;
        this.status = status;
        this.consumeLog = consumeLog;
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
     * 消息分片Key（用于结合“分区路由”生成 partitionId）
     */
    /*private int partitionId;*/
    private String partitionKey;

    /**
     * 消息数据
     */
    private String data;

    /**
     * 生效时间（时间戳；为空使用当前时间）
     */
    private long effectTime;

    /**
     * 状态
     *      MessageStatusEnum
     *          EXECUTE_SUCCESS(2, "执行成功"),
     *          EXECUTE_FAIL(3, "执行失败"),
     *          EXECUTE_TIMEOUT(4, "超时失败")
     */
    private int status;

    /**
     * 消费日志
     */
    private String consumeLog;

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

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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

    @Override
    public String toString() {
        return "MessageData{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", partitionKey='" + partitionKey + '\'' +
                ", data='" + data + '\'' +
                ", effectTime=" + effectTime +
                ", status=" + status +
                ", consumeLog='" + consumeLog + '\'' +
                '}';
    }
}
