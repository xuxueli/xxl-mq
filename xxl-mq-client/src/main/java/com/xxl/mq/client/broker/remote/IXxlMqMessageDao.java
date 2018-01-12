package com.xxl.mq.client.broker.remote;

import com.xxl.mq.client.message.XxlMqMessage;

import java.util.Date;
import java.util.List;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface IXxlMqMessageDao {

    // for admin
    public List<XxlMqMessage> pageList(int offset, int pagesize, String name, String status);
    public int pageListCount(int offset, int pagesize, String name, String status);

    public int delete(int id);
    public int update(int id, String data, Date delayTime, String status, String addMsg, int retryCount);

    // for broker
    public int save(XxlMqMessage xxlMqMessage);
    public int saveMsgList(List<XxlMqMessage> xxlMqMessagesList);
    public List<XxlMqMessage> pullNewMessage(String name, String newStatus, int pagesize, int consumerRank, int consumerTotal);

    public int lockMessage(int id, String addMsg, String fromStatus, String toStatus);
    public int updateStatus(int id, String status, String addMsg);

    public List<Integer> retryMessageIds(int pagesize, String failStatus);

    public int retryStatusFresh(int id, String addMsg, String failStatus, String newStatus);
}
