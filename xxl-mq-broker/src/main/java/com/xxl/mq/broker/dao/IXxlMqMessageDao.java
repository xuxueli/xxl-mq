package com.xxl.mq.broker.dao;

import com.xxl.mq.broker.core.model.XxlMqMessage;

import java.util.Date;
import java.util.List;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface IXxlMqMessageDao {

    public List<XxlMqMessage> pageList(int offset, int pagesize, String name, String status);
    public int pageListCount(int offset, int pagesize, String name, String status);

    public int delete(int id);
    public int update(int id, String data, Date delayTime, String status, String addMsg);

    public int save(XxlMqMessage xxlMqMessage);
    public List<XxlMqMessage> pullMessage(String name, String status, int pagesize, int consumerRank, int consumerTotal);

    public int lockMessage(int id, String status, String addMsg, String originStatus);
    public int updateStatus(int id, String status, String addMsg);

}
