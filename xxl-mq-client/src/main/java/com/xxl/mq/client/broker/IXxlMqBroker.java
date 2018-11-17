package com.xxl.mq.client.broker;

import com.xxl.mq.client.message.XxlMqMessage;

import java.util.List;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface IXxlMqBroker {

    /**
     * 新增消息，批量
     *
     * @param messages
     * @return
     */
    public int addMessages(List<XxlMqMessage> messages);

    /**
     * 分片数据，批量： MOD( "分片ID", #{consumerTotal}) = #{consumerRank}, 值 consumerTotal>1 时生效
     */
    public List<XxlMqMessage> pullNewMessage(String topic, String group, int consumerRank, int consumerTotal, int pagesize);

    /**
     *  锁定消息，单个；XxlMqMessageStatus：NEW >>> ING
     *
     *  @param id
     *  @param appendLog
     *  @return
     */
    public int lockMessage(long id, String appendLog);

    /**
     *  回调消息，批量；XxlMqMessageStatus：ING >>> SUCCESS/FAIL
     *
     * @param messages
     * @return
     */
    public int callbackMessages(List<XxlMqMessage> messages);

}
