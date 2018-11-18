package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.result.ReturnT;
import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.broker.service.IXxlMqMessageService;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.util.DateFormatUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

/**
 * Created by xuxueli on 16/8/28.
 */
@Service
public class XxlMqMessageServiceImpl implements IXxlMqMessageService {

    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;

    @Override
    public Map<String, Object> pageList(int offset, int pagesize, String name, String status) {

        List<XxlMqMessage> list = xxlMqMessageDao.pageList(offset, pagesize, name, status);
        int total = xxlMqMessageDao.pageListCount(offset, pagesize, name, status);

        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("data", list);
        maps.put("recordsTotal", total);
        maps.put("recordsFiltered", total);
        return maps;
    }

    @Override
    public ReturnT<String> delete(int id) {
        int ret = xxlMqMessageDao.delete(id);
        return ret>0 ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    @Override
    public ReturnT<String> update(int id, String data, String delayTimeStr, String status, int retryCount) {
        // id
        if (id<1){
            return new ReturnT<String>(500, "参数非法");
        }
        // data
        if (data == null){
            data = "";
        }
        // delayTime
        Date delayTime = null;
        try {
            delayTime = DateFormatUtil.parseDateTime(delayTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (delayTime==null) {
            return new ReturnT<String>(500, "消息数据格式不合法");
        }
        // status
        if (XxlMqMessageStatus.valueOf(status)==null) {
            return new ReturnT<String>(500, "消息状态不合法");
        }
        // retryCount
        if (retryCount<0) {
            retryCount = 0;
        }
        // addMsg
        String tim = null;
        try {
            tim = DateFormatUtil.formatDateTime(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String addMsg = "<hr>》》》时间:{"+ tim +"}<br>》》》操作:人工手工修改";

        // TODO
        XxlMqMessage mqMessage = new XxlMqMessage();

        int ret = xxlMqMessageDao.update(mqMessage);
        return ret>0 ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    @Override
    public ReturnT<String> add(String name, String data, String delayTimeStr, String status, int retryCount) {

        // name
        if (name==null || name.trim().length()==0) {
            return new ReturnT<String>(500, "消息主题不可为空");
        }
        if (name.length()>250) {
            return new ReturnT<String>(500, "消息主题长度超长");
        }
        // data
        if (data == null){
            data = "";
        }
        // delayTime
        Date delayTime = null;
        try {
            delayTime = DateFormatUtil.parseDateTime(delayTimeStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (delayTime==null) {
            return new ReturnT<String>(500, "消息数据格式不合法");
        }
        // status
        if (XxlMqMessageStatus.valueOf(status)==null) {
            return new ReturnT<String>(500, "消息状态不合法");
        }
        // retryCount
        if (retryCount<0) {
            retryCount = 0;
        }
        // addMsg
        String tim = null;
        try {
            tim = DateFormatUtil.formatDateTime(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String addMsg = "<hr>》》》时间:{"+ tim +"}<br>》》》操作:人工手工录入";


        XxlMqMessage msg = new XxlMqMessage();
        /*msg.setName(name);
        msg.setData(data);
        msg.setDelayTime(delayTime);
        msg.setStatus(status);
        msg.setMsg(addMsg);
        msg.setRetryCount(retryCount);*/


        // TODO

        xxlMqMessageDao.save(Arrays.asList(msg));
        return ReturnT.SUCCESS;
    }
}
