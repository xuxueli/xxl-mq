package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.model.XxlMqMessage;
import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.service.MessageManage;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by xuxueli on 16/8/28.
 */
@Service("messageManage")
public class MessageManageImpl implements MessageManage {

    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;


    @Override
    public LinkedList<Message> pageList(int pagesize, String name) {
        List<XxlMqMessage> list = xxlMqMessageDao.pageList(0, pagesize, name, "NEW");
        if (list!=null && list.size()>0) {

            LinkedList<Message> msgList = new LinkedList<Message>();
            for (XxlMqMessage messge : list) {
                messge.setStatus("RUN");
                xxlMqMessageDao.update(messge);

                Message msg = new Message(messge.getName(), new HashMap<String, String>());
                msgList.add(msg);
            }
            return msgList;
        }
        return null;
    }
}
