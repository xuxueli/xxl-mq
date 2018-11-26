package com.xxl.mq.admin.dao;

import com.xxl.mq.admin.core.model.XxlCommonRegistryData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xuxueli 2018-11-20
 */
@Mapper
public interface IXxlCommonRegistryDataDao {


    public int refresh(@Param("xxlCommonRegistryData") XxlCommonRegistryData xxlCommonRegistryData);

    public int add(@Param("xxlCommonRegistryData") XxlCommonRegistryData xxlCommonRegistryData);


    public List<XxlCommonRegistryData> findData(@Param("key") String key);

    public int cleanData(@Param("timeout") int timeout);

    public int deleteDataValue(@Param("key") String key,
                               @Param("value") String value);

    public int count();

}
