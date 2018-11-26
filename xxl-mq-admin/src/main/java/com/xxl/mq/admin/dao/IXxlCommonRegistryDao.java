package com.xxl.mq.admin.dao;

import com.xxl.mq.admin.core.model.XxlCommonRegistry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xuxueli 2018-11-20
 */
@Mapper
public interface IXxlCommonRegistryDao {

    public List<XxlCommonRegistry> pageList(@Param("offset") int offset, @Param("pagesize") int pagesize);

    public XxlCommonRegistry load(@Param("key") String key);

    public int add(@Param("xxlCommonRegistry") XxlCommonRegistry xxlCommonRegistry);

    public int update(@Param("xxlCommonRegistry") XxlCommonRegistry xxlCommonRegistry);

    public int cleanDead();

}
