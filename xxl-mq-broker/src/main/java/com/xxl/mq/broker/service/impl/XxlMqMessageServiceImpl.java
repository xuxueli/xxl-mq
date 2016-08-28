package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.model.XxlMqMessage;
import com.xxl.mq.broker.core.result.ReturnT;
import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.broker.service.IXxlMqMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
@Service
public class XxlMqMessageServiceImpl implements IXxlMqMessageService {

    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;

    @Override
    public Map<String, Object> pageList(int offset, int pagesize, String name, String status) {

        List<XxlMqMessage> list = xxlMqMessageDao.pageList(offset, pagesize, name, status);
        int total = xxlMqMessageDao.pageListCount(offset, pagesize, name, status);

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
}
