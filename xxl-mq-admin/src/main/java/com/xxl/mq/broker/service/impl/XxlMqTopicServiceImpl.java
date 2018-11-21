package com.xxl.mq.broker.service.impl;

import com.xxl.mq.broker.core.model.XxlMqTopic;
import com.xxl.mq.broker.core.result.ReturnT;
import com.xxl.mq.broker.dao.IXxlMqMessageDao;
import com.xxl.mq.broker.dao.IXxlMqTopicDao;
import com.xxl.mq.broker.service.IXxlMqTopicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuxueli 2016-5-28 15:30:33
 */
@Service
public class XxlMqTopicServiceImpl implements IXxlMqTopicService {
    private static Logger logger = LoggerFactory.getLogger(XxlMqTopicServiceImpl.class);


    @Resource
    private IXxlMqTopicDao xxlMqTopicDao;
    @Resource
    private IXxlMqMessageDao xxlMqMessageDao;


    @Override
    public Map<String, Object> pageList(int start, int length, int bizId, String topic) {
        // page list
        List<XxlMqTopic> list = xxlMqTopicDao.pageList(start, length, bizId, topic);
        int list_count = xxlMqTopicDao.pageListCount(start, length, bizId, topic);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        maps.put("data", list);  					// 分页列表
        return maps;
    }

    @Override
    public XxlMqTopic load(String topic) {
        return xxlMqTopicDao.load(topic);
    }

    @Override
    public ReturnT<String> add(XxlMqTopic xxlMqTopic) {

        // valid
        if (xxlMqTopic.getTopic()==null || xxlMqTopic.getTopic().trim().length()==0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "消息主题不可为空");
        }
        if (!(xxlMqTopic.getTopic().length()>=4 && xxlMqTopic.getTopic().length()<=255)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "消息主题长度非法[4~255]");
        }

        // exist
        XxlMqTopic exist = xxlMqTopicDao.load(xxlMqTopic.getTopic());
        if (exist != null) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "消息主题不可重复");
        }


        int ret = xxlMqTopicDao.add(xxlMqTopic);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }


    @Override
    public ReturnT<String> update(XxlMqTopic xxlMqTopic) {

        // valid
        if (xxlMqTopic.getTopic()==null || xxlMqTopic.getTopic().trim().length()==0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "消息主题不可为空");
        }
        if (!(xxlMqTopic.getTopic().length()>=4 && xxlMqTopic.getTopic().length()<=255)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "消息主题长度非法[4~255]");
        }

        int ret = xxlMqTopicDao.update(xxlMqTopic);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    @Override
    public ReturnT<String> delete(String topic) {

        // valid, limit use
        int count = xxlMqMessageDao.pageListCount(0, 1, topic, null, null, null);
        if (count > 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "禁止删除，该Topic下存在消息");
        }


        int ret = xxlMqTopicDao.delete(topic);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

}
