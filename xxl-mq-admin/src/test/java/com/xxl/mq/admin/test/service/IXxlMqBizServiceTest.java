package com.xxl.mq.admin.test.service;

import com.xxl.mq.admin.core.model.XxlMqBiz;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.service.IXxlMqBizService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IXxlMqBizServiceTest {

    @Resource
    private IXxlMqBizService xxlMqBizService;

    @Test
    public void test(){

        XxlMqBiz biz = new XxlMqBiz();
        biz.setBizName("biz");
        biz.setOrder(1);
        ReturnT<String> addRet = xxlMqBizService.add(biz);

        List<XxlMqBiz> alllist = xxlMqBizService.findAll();

        XxlMqBiz biz2 = xxlMqBizService.load(biz.getId());

        biz2.setBizName("biz2");
        biz2.setOrder(2);
        ReturnT<String> updateRet = xxlMqBizService.update(biz2);

        ReturnT<String> delRet = xxlMqBizService.delete(biz.getId());
    }

}
