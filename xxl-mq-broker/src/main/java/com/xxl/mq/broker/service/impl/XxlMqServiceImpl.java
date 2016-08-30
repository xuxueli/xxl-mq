package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.model.XxlMqMessage;
import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.util.JacksonUtil;
import com.xxl.mq.client.service.XxlMqService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqServiceImpl implements XxlMqService {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqServiceImpl.class);

    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;

    @Override
    public void saveMessage(Message message) {
        if (message == null || StringUtils.isBlank(message.getName())) {
            return;
        }

        XxlMqMessage msg = new XxlMqMessage();
        msg.setName(message.getName());
        msg.setDestination(message.getDestination().name());
        msg.setData(JacksonUtil.writeValueAsString(message.getData()));
        msg.setDelayTime(message.getDelayTime());
        msg.setAddTime(new Date());
        msg.setUpdateTime(new Date());
        msg.setStatus(message.getStatus().name());
        msg.setMsg(null);

        xxlMqMessageDao.save(msg);
    }



    @Override
    public LinkedList<Message> pageList(int pagesize, String name) {
        List<XxlMqMessage> list = xxlMqMessageDao.pageList(0, pagesize, name, Message.Status.NEW.name());
        if (list!=null && list.size()>0) {

            LinkedList<Message> msgList = new LinkedList<Message>();
            for (XxlMqMessage xxlMqMessage : list) {
                // update
                xxlMqMessage.setStatus(Message.Status.ING.name());
                xxlMqMessageDao.update(xxlMqMessage);

                Message msg = new Message();
                msg.setId(xxlMqMessage.getId());
                msg.setName(xxlMqMessage.getName());
                msg.setData(JacksonUtil.readValue(xxlMqMessage.getData(), Map.class));

                msgList.add(msg);

            }
            return msgList;
        }
        return null;
    }

}
