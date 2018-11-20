package com.xxl.mq.broker.dao;

import com.xxl.mq.broker.core.model.XxlMqBiz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xuxueli 2018-11-20
 */
@Mapper
public interface IXxlMqBizDao {

    public List<XxlMqBiz> findAll();

    public XxlMqBiz load(@Param("id") int id);

    public int add(XxlMqBiz xxlMqBiz);

    public int update(@Param("xxlMqBiz") XxlMqBiz xxlMqBiz);

    public int delete(@Param("id") int id);

}
