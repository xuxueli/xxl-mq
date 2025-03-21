package com.xxl.mq.admin.mapper;

import com.xxl.mq.admin.model.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

/**
* Message Mapper
*
* Created by xuxueli on '2025-03-21 21:54:06'.
*/
@Mapper
public interface MessageMapper {

    /**
    * 新增
    */
    public int insert(@Param("message") Message message);

    /**
    * 删除
    */
    public int delete(@Param("ids") List<Integer> ids);

    /**
    * 更新
    */
    public int update(@Param("message") Message message);

    /**
    * Load查询
    */
    public Message load(@Param("id") int id);

    /**
    * 分页查询Data
    */
	public List<Message> pageList(@Param("offset") int offset,
                                  @Param("pagesize") int pagesize,
                                  @Param("topic") String topic,
                                  @Param("effectTimeStart") Date effectTimeStart,
                                  @Param("effectTimeEnd") Date effectTimeEnd);

    /**
    * 分页查询Count
    */
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("topic") String topic,
                             @Param("effectTimeStart") Date effectTimeStart,
                             @Param("effectTimeEnd") Date effectTimeEnd);

}
