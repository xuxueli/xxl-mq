package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.core.model.XxlMqBiz;
import com.xxl.mq.admin.core.result.ReturnT;
import com.xxl.mq.admin.dao.IXxlMqBizDao;
import com.xxl.mq.admin.dao.IXxlMqTopicDao;
import com.xxl.mq.admin.service.IXxlMqBizService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author xuxueli 2018-11-20
 */
@Service
public class XxlMqBizServiceImpl implements IXxlMqBizService {

    @Resource
    private IXxlMqBizDao xxlMqBizDao;
    @Resource
    private IXxlMqTopicDao xxlMqTopicDao;


    @Override
    public List<XxlMqBiz> findAll() {
        return xxlMqBizDao.findAll();
    }

    @Override
    public XxlMqBiz load(int id) {
        return xxlMqBizDao.load(id);
    }

    @Override
    public ReturnT<String> add(XxlMqBiz xxlMqBiz) {

        // valid
        if (xxlMqBiz.getBizName()==null || xxlMqBiz.getBizName().trim().length()==0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "业务线名称不可为空");
        }
        if (!(xxlMqBiz.getBizName().trim().length()>=4 && xxlMqBiz.getBizName().trim().length()<=64)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "业务线名称长度非法[2-64]");
        }

        // exist
        List<XxlMqBiz> list = findAll();
        if (list != null) {
            for (XxlMqBiz item: list) {
                if (item.getBizName().equals(xxlMqBiz.getBizName())) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "业务线名称不可重复");
                }
            }
        }

        int ret = xxlMqBizDao.add(xxlMqBiz);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    @Override
    public ReturnT<String> update(XxlMqBiz xxlMqBiz) {

        // valid
        if (xxlMqBiz.getBizName()==null || xxlMqBiz.getBizName().trim().length()==0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "业务线名称不可为空");
        }
        if (!(xxlMqBiz.getBizName().trim().length()>=4 && xxlMqBiz.getBizName().trim().length()<=64)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "业务线名称长度非法[2-64]");
        }

        // exist
        List<XxlMqBiz> list = findAll();
        if (list != null) {
            for (XxlMqBiz item: list) {
                if (item.getId()!=xxlMqBiz.getId() && item.getBizName().equals(xxlMqBiz.getBizName())) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "业务线名称不可重复");
                }
            }
        }

        int ret = xxlMqBizDao.update(xxlMqBiz);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

    @Override
    public ReturnT<String> delete(int id) {

        // valid limit not use
        int count = xxlMqTopicDao.pageListCount(0, 1, id, null);
        if (count > 0) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "禁止删除，该业务线下存在Topic");
        }

        int ret = xxlMqBizDao.delete(id);
        return ret>0?ReturnT.SUCCESS:ReturnT.FAIL;
    }

}
