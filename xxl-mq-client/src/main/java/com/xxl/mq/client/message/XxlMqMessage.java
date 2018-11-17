package com.xxl.mq.client.message;

import java.io.Serializable;
import java.util.Date;

/**
 * 消息对象
 * @author xuxueli 2016-08-28 14:05:23
 */
public class XxlMqMessage implements Serializable {
	private static final long serialVersionUID = 42L;

	private int id;
	private String name;		// 消息主题
	private String data;		// 消息数据, Map<String, String>对象系列化的JSON字符串
	private Date delayTime;		// 延迟执行的时间, new Date()立即执行, 否则在延迟时间点之后开始执行;
	private Date addTime;		// 创建时间
	private Date updateTime;	// 更新时间
	private String status;		// 消息状态: NEW=新消息、ING=消费中、SUCCESS=消费成功、FAIL=消费失败
	private String msg;			// 历史流转日志
	private int retryCount;		// 剩余重试次数, 默认0不重试


	// TODO
	private String group;

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

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Date getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(Date delayTime) {
		this.delayTime = delayTime;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
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

	@Override
	public String toString() {
		return "XxlMqMessage{" +
				"id=" + id +
				", name='" + name + '\'' +
				", data='" + data + '\'' +
				", delayTime=" + delayTime +
				", addTime=" + addTime +
				", updateTime=" + updateTime +
				", status='" + status + '\'' +
				", msg='" + msg + '\'' +
				", retryCount=" + retryCount +
				'}';
	}

}
