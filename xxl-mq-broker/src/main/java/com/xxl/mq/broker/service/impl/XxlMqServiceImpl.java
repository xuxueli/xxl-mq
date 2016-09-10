package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.model.XxlMqMessage;
import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.broker.biz.MqBrokerService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqServiceImpl implements MqBrokerService {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqServiceImpl.class);

    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;

    @Override
    public int saveMessage(Message message) {
        if (message == null || StringUtils.isBlank(message.getName())) {
            return -1;
        }

        XxlMqMessage msg = new XxlMqMessage();
        msg.setName(message.getName());
        msg.setData(message.getData());
        msg.setDelayTime(message.getDelayTime());
        msg.setStatus(message.getStatus());
        msg.setMsg(message.getMsg());
        msg.setRetryCount(message.getRetryCount());

        return xxlMqMessageDao.save(msg);
    }

    @Override
    public LinkedList<Message> pullNewMessage(String name, int pagesize, int consumerRank, int consumerTotal) {
        List<XxlMqMessage> list = xxlMqMessageDao.pullNewMessage(name, Message.Status.NEW.name(), pagesize, consumerRank, consumerTotal);
        if (list!=null && list.size()>0) {

            LinkedList<Message> msgList = new LinkedList<Message>();
            for (XxlMqMessage xxlMqMessage : list) {
                Message msg = new Message();
                msg.setId(xxlMqMessage.getId());
                msg.setName(xxlMqMessage.getName());
                msg.setData(xxlMqMessage.getData());

                msgList.add(msg);
            }
            return msgList;
        }
        return null;
    }

    @Override
    public int lockMessage(int id, String addMsg) {
        return xxlMqMessageDao.lockMessage(id, addMsg, Message.Status.NEW.name(), Message.Status.ING.name());
    }

    @Override
    public int consumeCallbackMessage(Message message) {
        return xxlMqMessageDao.updateStatus(message.getId(), message.getStatus(), message.getMsg());
    }

}
