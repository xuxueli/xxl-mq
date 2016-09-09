package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.model.XxlMqMessage;
import com.xxl.mq.broker.core.result.ReturnT;
import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.broker.service.IXxlMqMessageService;
import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.util.DateFormatUtil;
import com.xxl.mq.client.rpc.util.JacksonUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ReturnT<String> update(int id, String data, String delayTimeStr, String status) {
        // id
        if (id<1){
            return new ReturnT<String>(500, "参数非法");
        }
        // data
        if (StringUtils.isNotBlank(data)){
            Map<String,String> dataMap = JacksonUtil.readValue(data, Map.class);
            if (dataMap==null) {
                return new ReturnT<String>(500, "消息数据格式不合法");
            }
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
        if (Message.Status.valueOf(status)==null) {
            return new ReturnT<String>(500, "消息状态不合法");
        }
        String tim = null;
        try {
            tim = DateFormatUtil.formatDateTime(new Date());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String addMsg = "<hr>》》》时间:{"+ tim +"}<br>》》》操作:人工手工修改";
        int ret = xxlMqMessageDao.update(id ,data, delayTime, status, addMsg);
        return ret>0 ? ReturnT.SUCCESS : ReturnT.FAIL;
    }
}
