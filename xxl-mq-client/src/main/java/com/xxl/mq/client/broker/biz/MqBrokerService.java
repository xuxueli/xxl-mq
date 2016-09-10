package com.xxl.mq.client.broker.biz;

import com.xxl.mq.client.message.Message;

import java.util.LinkedList;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface MqBrokerService {

    public int saveMessage(Message message);

    /**
     * 分布式获取分配给自己的数据: MOD(id, #{consumerTotal}) = #{consumerRank}, 当值为 consumerTotal>1 时生效
     */
    public LinkedList<Message> pullNewMessage(String name, int pagesize, int consumerRank, int consumerTotal);

    /**
     * 消费者,锁定一条消息 (NEW >>> ING)
     * @param id
     * @param addMsg
     * @return
     */
    public int lockMessage(int id, String addMsg);

    /**
     * 消费者,消费结果回调 (ING >>> SUCCESS/FAIL)
     * @param message
     * @return
     */
    public int consumeCallbackMessage(Message message);

}
