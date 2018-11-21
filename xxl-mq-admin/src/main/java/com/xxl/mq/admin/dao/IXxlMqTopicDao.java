package com.xxl.mq.admin.dao;

import com.xxl.mq.admin.core.model.XxlMqTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xuxueli 2018-11-20
 */
@Mapper
public interface IXxlMqTopicDao {

    public List<XxlMqTopic> pageList(@Param("offset") int offset,
                                       @Param("pagesize") int pagesize,
                                       @Param("bizId") int bizId,
                                       @Param("topic") String topic);
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("bizId") int bizId,
                             @Param("topic") String topic);

    public XxlMqTopic load(@Param("topic") String topic);

    public int add(@Param("xxlMqTopic") XxlMqTopic xxlMqTopic);

    public int update(@Param("xxlMqTopic") XxlMqTopic xxlMqTopic);

    public int delete(@Param("topic") String topic);

}
