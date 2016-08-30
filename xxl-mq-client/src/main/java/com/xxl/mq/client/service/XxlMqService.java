package com.xxl.mq.client.service;

import com.xxl.mq.client.message.Message;

import java.util.LinkedList;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface XxlMqService {

    public int saveMessage(Message message);
    public int updateMessage(Message message);

    /**
     * 分布式获取分配给自己的数据: MOD(id, #{consumerTotal}) = #{consumerRank}, 当值为 consumerTotal>1 时生效
     */
    public LinkedList<Message> pullMessage(String name, String status, int pagesize, int consumerRank, int consumerTotal);
}
