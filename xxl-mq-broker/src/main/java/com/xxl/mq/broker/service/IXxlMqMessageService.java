package com.xxl.mq.broker.service;

import com.xxl.mq.broker.core.result.ReturnT;

import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface IXxlMqMessageService {

    public Map<String,Object> pageList(int offset, int pagesize, String name, String status);

    public ReturnT<String> delete(int id);

    public ReturnT<String> update(int id, String data, String delayTimeStr, String status);

}