package com.xxl.mq.client.service;

import com.xxl.mq.client.message.Message;

import java.util.LinkedList;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface XxlMqService {

    public int saveMessage(Message message);
    public int updateMessage(Message message);

    public LinkedList<Message> pageList(int pagesize, String name);
}
