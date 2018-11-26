package com.xxl.mq.admin.dao;

import com.xxl.mq.admin.core.model.XxlCommonRegistryMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xuxueli 2018-11-20
 */
@Mapper
public interface IXxlCommonRegistryMessageDao {

    public int add(@Param("xxlCommonRegistryMessage") XxlCommonRegistryMessage xxlCommonRegistryMessage);

    public List<XxlCommonRegistryMessage> findMessage(@Param("excludeIds") List<Integer> excludeIds);

    public int cleanMessage(@Param("messageTimeout") int messageTimeout);

}
