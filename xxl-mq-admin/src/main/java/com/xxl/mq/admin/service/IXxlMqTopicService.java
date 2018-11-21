package com.xxl.mq.admin.service;

import com.xxl.mq.admin.core.model.XxlMqTopic;
import com.xxl.mq.admin.core.result.ReturnT;

import java.util.Map;

/**
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface IXxlMqTopicService {

    public Map<String, Object> pageList(int start, int length, int bizId, String topic);

    public XxlMqTopic load(String topic);

    public ReturnT<String> add(XxlMqTopic xxlMqTopic);

    public ReturnT<String> update(XxlMqTopic xxlMqTopic);

    public ReturnT<String> delete(String topic);


}
