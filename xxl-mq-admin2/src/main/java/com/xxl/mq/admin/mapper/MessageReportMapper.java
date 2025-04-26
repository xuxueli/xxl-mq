package com.xxl.mq.admin.mapper;

import com.xxl.mq.admin.model.entity.MessageReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
* MessageReportMapper
*
* Created by xuxueli on 2025-04-26
*/
@Mapper
public interface MessageReportMapper {

    /**
    * 新增
    */
    public int insert(@Param("messageReport") MessageReport messageReport);

    /**
    * 更新
    */
    public int update(@Param("messageReport") MessageReport messageReport);

    /**
    * Load查询
    */
    public MessageReport load(@Param("produceDay") Date produceDay);

}
