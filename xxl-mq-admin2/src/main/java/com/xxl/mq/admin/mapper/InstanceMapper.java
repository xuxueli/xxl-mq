package com.xxl.mq.admin.mapper;

import com.xxl.mq.admin.model.entity.Instance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
* Instance Mapper
*
* Created by xuxueli on '2025-03-16 12:29:49'.
*/
@Mapper
public interface InstanceMapper {

    /**
    * 新增
    */
    public int insert(@Param("instance") Instance instance);

    /**
     * 插入或更新
     *
     * @param instance
     * @return
     */
    public int insertOrUpdate(@Param("instance") Instance instance);

    /**
    * 删除
    */
    public int delete(@Param("ids") List<Integer> ids);

    /**
    * 更新
    */
    public int update(@Param("instance") Instance instance);

    /**
    * Load查询
    */
    public Instance load(@Param("id") int id);

    /**
    * 分页查询Data
    */
	public List<Instance> pageList(@Param("offset") int offset, @Param("pagesize") int pagesize);

    /**
    * 分页查询Count
    */
    public int pageListCount(@Param("offset") int offset, @Param("pagesize") int pagesize);

    /**
     * 查询在线实例
     *
     * @param effectiveTime
     * @return
     */
    public List<Instance> queryOnlineInstance(@Param("effectiveTime") Date effectiveTime);

}
