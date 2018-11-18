package com.xxl.mq.broker.dao.impl;

import com.xxl.mq.client.broker.remote.IXxlMqMessageDao;
import com.xxl.mq.client.message.XxlMqMessage;
import org.mybatis.spring.SqlSessionTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqMessageDaoImpl implements IXxlMqMessageDao {

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;


    @Override
    public List<XxlMqMessage> pageList(int offset, int pagesize, String name, String status) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("offset", offset);
        params.put("pagesize", pagesize);
        params.put("name", name);
        params.put("status", status);

        return sqlSessionTemplate.selectList("XxlMqMessageMapper.pageList", params);
    }

    @Override
    public int pageListCount(int offset, int pagesize, String name, String status) {

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("offset", offset);
        params.put("pagesize", pagesize);
        params.put("name", name);
        params.put("status", status);

        return sqlSessionTemplate.selectOne("XxlMqMessageMapper.pageListCount", params);
    }

    @Override
    public int delete(int id) {
        return sqlSessionTemplate.delete("XxlMqMessageMapper.delete", id);
    }

    @Override
    public int update(int id, String data, Date delayTime, String status, String addMsg, int retryCount) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("data", data);
        params.put("delayTime", delayTime);
        params.put("status", status);
        params.put("addMsg", addMsg);
        params.put("retryCount", retryCount);

        return sqlSessionTemplate.delete("XxlMqMessageMapper.update", params);
    }

    @Override
    public int save(XxlMqMessage xxlMqMessage) {
        return sqlSessionTemplate.insert("XxlMqMessageMapper.save", xxlMqMessage);
    }

    @Override
    public List<XxlMqMessage> pullNewMessage(String name, String newStatus, int pagesize, int consumerRank, int consumerTotal) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name);
        params.put("newStatus", newStatus);
        params.put("pagesize", pagesize);
        params.put("consumerRank", consumerRank);
        params.put("consumerTotal", consumerTotal);

        return sqlSessionTemplate.selectList("XxlMqMessageMapper.pullNewMessage", params);
    }

    @Override
    public int lockMessage(int id, String addMsg, String fromStatus, String toStatus) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("addMsg", addMsg);
        params.put("fromStatus", fromStatus);
        params.put("toStatus", toStatus);

        return sqlSessionTemplate.update("XxlMqMessageMapper.lockMessage", params);
    }

    @Override
    public int updateStatus(int id, String status, String addMsg) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("status", status);
        params.put("addMsg", addMsg);

        return sqlSessionTemplate.update("XxlMqMessageMapper.updateStatus", params);
    }

    @Override
    public List<Integer> retryMessageIds(int pagesize, String failStatus) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("pagesize", pagesize);
        params.put("failStatus", failStatus);

        return sqlSessionTemplate.selectList("XxlMqMessageMapper.pagelistRetryMessage", params);
    }

    @Override
    public int retryStatusFresh(int id, String addMsg, String failStatus, String newStatus) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("addMsg", addMsg);
        params.put("failStatus", failStatus);
        params.put("newStatus", newStatus);

        return sqlSessionTemplate.update("XxlMqMessageMapper.retryStatusFresh", params);
    }

}
