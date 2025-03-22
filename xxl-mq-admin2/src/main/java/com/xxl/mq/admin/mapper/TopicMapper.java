package com.xxl.mq.admin.mapper;

import com.xxl.mq.admin.model.entity.Topic;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* Topic Mapper
*
* Created by xuxueli on '2025-03-21 12:52:25'.
*/
@Mapper
public interface TopicMapper {

    /**
    * 新增
    */
    public int insert(@Param("topic") Topic topic);

    /**
    * 删除
    */
    public int delete(@Param("ids") List<Integer> ids);

    /**
    * 更新
    */
    public int update(@Param("topic") Topic topic);

    /**
    * Load查询
    */
    public Topic load(@Param("id") int id);

    /**
     * 根据topic查询
     */
    public Topic loadByTopic(@Param("topic") String topic);

    /**
    * 分页查询Data
    */
	public List<Topic> pageList(@Param("offset") int offset,
                                @Param("pagesize") int pagesize,
                                @Param("appname") String appname,
                                @Param("topic") String topic);

    /**
    * 分页查询Count
    */
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("appname") String appname,
                             @Param("topic") String topic);

    /**
     * 批量更新状态
     */
    public int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

    /**
     * 查询总数
     */
    public int count();

}
