package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.model.XxlMqMessage;
import com.xxl.mq.broker.dao.impl.XxlMqMessageDaoImpl;
import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.service.BrokerService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by xuxueli on 16/8/28.
 */
@Service("brokerService")
public class BrokerServiceImpl implements BrokerService {

    @Resource
    private XxlMqMessageDaoImpl xxlMqMessageDao;

    @Override
    public void saveMessage(Message message) {
        if (message == null || StringUtils.isBlank(message.getName())) {
            return;
        }

        XxlMqMessage msg = new XxlMqMessage();
        msg.setName(message.getName());
        msg.setDestination(message.getName());
        msg.setData(message.getName());
        msg.setDelayTime(new Date());
        msg.setAddTime(new Date());
        msg.setUpdateTime(new Date());
        msg.setStatus(message.getName());
        msg.setMsg(message.getName());

        int id = xxlMqMessageDao.save(msg);
    }
}
