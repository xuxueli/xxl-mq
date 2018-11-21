package com.xxl.mq.broker.service;

import com.xxl.mq.broker.core.result.ReturnT;
import com.xxl.mq.client.message.XxlMqMessage;

import java.util.Date;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface IXxlMqMessageService {

    public Map<String,Object> pageList(int offset, int pagesize, String topic, String status, Date addTimeStart, Date addTimeEnd);

    public ReturnT<String> delete(int id);

    public ReturnT<String> update(XxlMqMessage message);

    public ReturnT<String> add(XxlMqMessage message);

    public Map<String,Object> dashboardInfo();

    public ReturnT<Map<String,Object>> chartInfo(Date startDate, Date endDate);

    public ReturnT<String> clearMessage(String topic, String status, int type);

}