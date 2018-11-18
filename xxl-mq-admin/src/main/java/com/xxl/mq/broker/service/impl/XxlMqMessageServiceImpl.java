package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.result.ReturnT;
import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.broker.service.IXxlMqMessageService;
import com.xxl.mq.client.consumer.registry.ConsumerRegistryHelper;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import com.xxl.mq.client.util.DateFormatUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by xuxueli on 16/8/28.
 */
@Service
public class XxlMqMessageServiceImpl implements IXxlMqMessageService {

    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;

    @Override
    public Map<String, Object> pageList(int offset, int pagesize, String topic, String status) {

        List<XxlMqMessage> list = xxlMqMessageDao.pageList(offset, pagesize, topic, status);
        int total = xxlMqMessageDao.pageListCount(offset, pagesize, topic, status);

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
    public ReturnT<String> update(XxlMqMessage message) {

        // valid id
        if (message.getId() < 1){
            return new ReturnT<String>(500, "参数非法");
        }

        // valid message
        message.setLog("<hr>》》》时间:{"+ DateFormatUtil.getNowTime() +"}<br>》》》操作:人工手工修改");
        ReturnT<String> validRet = validMessage(message);
        if (validRet != null) {
            return validRet;
        }

        // update
        int ret = xxlMqMessageDao.update(message);
        return ret>0 ? ReturnT.SUCCESS : ReturnT.FAIL;
    }

    private static ReturnT<String> validMessage(XxlMqMessage mqMessage){

        if (mqMessage.getId() > 0) {
            // topic
            if (mqMessage.getTopic()==null || mqMessage.getTopic().trim().length()==0 || mqMessage.getTopic().length()>512) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "topic 格式非法." );
            }

            // group
            if (mqMessage.getTopic()==null || mqMessage.getTopic().trim().length()==0) {
                mqMessage.setTopic(ConsumerRegistryHelper.DEFAULT_GROUP);
            }
            if (mqMessage.getTopic().length() > 256) {
                return new ReturnT<String>(ReturnT.FAIL_CODE, "group 格式非法." );
            }
        }

        // data
        if (mqMessage.getData() == null) {
            mqMessage.setData("");
        }

        // status
        if (XxlMqMessageStatus.valueOf(mqMessage.getStatus()) == null) {
            return new ReturnT<String>(500, "消息状态非法");
        }

        // retryCount
        if (mqMessage.getRetryCount() < 0) {
            mqMessage.setRetryCount(0);
        }

        // shardingId
        if (mqMessage.getShardingId() < 0) {
            mqMessage.setShardingId(0);
        }

        // effectTime
        if (mqMessage.getEffectTime() == null) {
            mqMessage.setEffectTime(new Date());
        }

        // timeout
        if (mqMessage.getTimeout() < 0) {
            mqMessage.setTimeout(0);
        }

        // log

        return null;
    }

    @Override
    public ReturnT<String> add(XxlMqMessage message) {

        // valid message
        message.setLog("<hr>》》》时间:{"+ DateFormatUtil.getNowTime() +"}<br>》》》操作:人工手工录入");
        ReturnT<String> validRet = validMessage(message);
        if (validRet != null) {
            return validRet;
        }

        // save
        xxlMqMessageDao.save(Arrays.asList(message));
        return ReturnT.SUCCESS;
    }
}
