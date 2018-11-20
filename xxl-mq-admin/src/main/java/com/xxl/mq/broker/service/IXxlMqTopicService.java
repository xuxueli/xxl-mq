package com.xxl.mq.broker.service;

import com.xxl.mq.broker.core.model.XxlMqTopic;
import com.xxl.mq.broker.core.result.ReturnT;

import java.util.Map;

/**
 * core job action for xxl-job
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface IXxlMqTopicService {

    public Map<String, Object> pageList(int start, int length, int bizId, String topic);

    public XxlMqTopic load(String topic);

    public ReturnT<String> add(XxlMqTopic xxlMqTopic);

    public ReturnT<String> update(XxlMqTopic xxlMqTopic);

    public ReturnT<String> delete(String topic);


}
