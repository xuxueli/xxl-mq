package com.xxl.mq.broker.dao;

import com.xxl.mq.client.message.XxlMqMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * Created by xuxueli on 16/8/28.
 */
@Mapper
public interface IXxlMqMessageDao {

    // for admin
    public List<XxlMqMessage> pageList(@Param("offset") int offset,
                                       @Param("pagesize") int pagesize,
                                       @Param("topic") String topic,
                                       @Param("status") String status);
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("topic") String topic,
                             @Param("status") String status);

    public int delete(@Param("id") int id);

    public int update(@Param("message") XxlMqMessage message);

    // for broker
    public int save(@Param("messageList") List<XxlMqMessage> messageList);

    public List<XxlMqMessage> pullNewMessage(@Param("newStatus") String newStatus,
                                             @Param("topic") String topic,
                                             @Param("group") String group,
                                             @Param("consumerRank") int consumerRank,
                                             @Param("consumerTotal") int consumerTotal,
                                             @Param("pagesize") int pagesize);

    public int lockMessage(@Param("id") long id,
                           @Param("appendLog") String appendLog,
                           @Param("newStatus") String newStatus,
                           @Param("ingStatus") String ingStatus);

    public int updateStatus(@Param("messageList") List<XxlMqMessage> messageList);

    public int updateRetryCount(@Param("failStatus") String failStatus,
                                @Param("newStatus") String newStatus,
                                @Param("appendLog") String appendLog,
                                @Param("pagesize") int pagesize);

    public int cleanSuccessMessage(@Param("successStatus") String successStatus, @Param("logretentiondays") int logretentiondays);

}
