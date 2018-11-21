package com.xxl.mq.admin.test.service;

import com.xxl.mq.admin.core.model.XxlMqTopic;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.service.IXxlMqTopicService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IXxlMqTopicServiceTest {

    @Resource
    private IXxlMqTopicService xxlMqTopicService;

    @Test
    public void test(){

        XxlMqTopic topic = new XxlMqTopic();
        topic.setTopic("t");
        topic.setBizId(0);
        topic.setAuthor("zhangsan");
        topic.setAlarmEmails("xx@xx.com");
        ReturnT<String> addRet = xxlMqTopicService.add(topic);

        Map<String, Object> page = xxlMqTopicService.pageList(0, 10, -1, null);

        XxlMqTopic topic2 = xxlMqTopicService.load(topic.getTopic());

        topic2.setBizId(2);
        topic2.setAuthor("zhangsan2");
        topic2.setAlarmEmails("xx2@xx.com");
        ReturnT<String> updateRet = xxlMqTopicService.update(topic2);

       ReturnT<String> delRet = xxlMqTopicService.delete(topic.getTopic());
    }

}
