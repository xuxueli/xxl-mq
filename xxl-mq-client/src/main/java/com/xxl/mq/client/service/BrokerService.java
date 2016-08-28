package com.xxl.mq.client.service;

import com.xxl.mq.client.message.Message;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface BrokerService {

    public void saveMessage(Message message);

}
