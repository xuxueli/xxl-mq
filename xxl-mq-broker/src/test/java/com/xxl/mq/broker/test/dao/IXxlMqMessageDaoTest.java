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

    // admin
    @Test
    public void adminTest(){
        List<XxlMqMessage> pagelist = xxlMqMessageDao.pageList(0, 10, "t", XxlMqMessageStatus.NEW.name());
        int pagecount = xxlMqMessageDao.pageListCount(0, 10, "t", XxlMqMessageStatus.NEW.name());

        XxlMqMessage message = pagelist.get(0);
        message.setData("d5");
        message.setStatus(XxlMqMessageStatus.FAIL.name());
        message.setRetryCount(5);
        message.setShardingId(5);
        message.setEffectTime(new Date());
        message.setTimeout(5);
        message.setLog("555");

        int updateRet = xxlMqMessageDao.update(message);

        int delRet = xxlMqMessageDao.delete(1);
    }


    // broker

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

    @Test
    public void lockMessageTest(){
        int ret = xxlMqMessageDao.lockMessage(1, "appendLog", XxlMqMessageStatus.NEW.name(), XxlMqMessageStatus.ING.name());
    }

    @Test
    public void updateStatusest(){
        List<XxlMqMessage> messageList = xxlMqMessageDao.pullNewMessage(XxlMqMessageStatus.NEW.name(), "t", "g", 1, 0, 10);

        for (XxlMqMessage message:messageList) {
            message.setStatus(XxlMqMessageStatus.FAIL.name());
            message.setLog("+updateStatus");
        }
        int ret = xxlMqMessageDao.updateStatus(messageList);
    }

    @Test
    public void updateRetryCountTest(){
        int ret = xxlMqMessageDao.updateRetryCount(XxlMqMessageStatus.FAIL.name(), XxlMqMessageStatus.NEW.name(), "+updateRetryCount");
    }

    @Test
    public void cleanSuccessMessageTest(){
        int ret = xxlMqMessageDao.cleanSuccessMessage(XxlMqMessageStatus.SUCCESS.name(), 3);
    }

}
