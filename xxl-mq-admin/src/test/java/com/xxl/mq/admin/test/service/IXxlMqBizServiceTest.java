package com.xxl.mq.admin.test.service;

import com.xxl.mq.admin.core.model.XxlMqBiz;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.service.IXxlMqBizService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
