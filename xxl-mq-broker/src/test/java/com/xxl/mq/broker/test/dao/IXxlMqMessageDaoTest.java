package com.xxl.mq.broker.test.dao;

import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.message.XxlMqMessageStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IXxlMqMessageDaoTest {

    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;

    @Test
    public void saveTest(){

        List<XxlMqMessage> messageList = new ArrayList<>();

        // make
        XxlMqMessage mqMessage = new XxlMqMessage();
        mqMessage.setTopic("t");
        mqMessage.setGroup("g");
        mqMessage.setData("d");
        mqMessage.setStatus(XxlMqMessageStatus.NEW.name());
        mqMessage.setRetryCount(1);
        mqMessage.setShardingId(1);
        mqMessage.setEffectTime(new Date());
        mqMessage.setTimeout(1);
        mqMessage.setLog("1");

        XxlMqMessage mqMessage2 = new XxlMqMessage();
        mqMessage2.setTopic("t2");
        mqMessage2.setGroup("g2");
        mqMessage2.setData("d2");
        mqMessage2.setStatus(XxlMqMessageStatus.FAIL.name());
        mqMessage2.setRetryCount(2);
        mqMessage2.setShardingId(2);
        mqMessage2.setEffectTime(new Date());
        mqMessage2.setTimeout(2);
        mqMessage2.setLog("2");

        messageList.add(mqMessage);
        messageList.add(mqMessage2);

        int ret = xxlMqMessageDao.save(messageList);
    }


    @Test
    public void pullNewMessageTest(){
        List<XxlMqMessage> messageList = xxlMqMessageDao.pullNewMessage(XxlMqMessageStatus.NEW.name(), "t1", null, 1, 2, 10);
    }


}
