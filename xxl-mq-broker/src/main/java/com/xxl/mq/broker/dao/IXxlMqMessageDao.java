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
                                       @Param("name") String name,
                                       @Param("status") String status);
    public int pageListCount(@Param("offset") int offset,
                             @Param("pagesize") int pagesize,
                             @Param("name") String name,
                             @Param("status") String status);

    public int delete(@Param("id") int id);
    public int update(@Param("id") int id,
                      @Param("data") String data,
                      @Param("delayTime") Date delayTime,
                      @Param("status") String status,
                      @Param("addMsg") String addMsg,
                      @Param("retryCount") int retryCount);

    // for broker
    public int save(@Param("xxlMqMessage") XxlMqMessage xxlMqMessage);
    public List<XxlMqMessage> pullNewMessage(@Param("name") String name,
                                             @Param("newStatus") String newStatus,
                                             @Param("pagesize") int pagesize,
                                             @Param("consumerRank") int consumerRank,
                                             @Param("consumerTotal") int consumerTotal);

    public int lockMessage(@Param("id") int id,
                           @Param("addMsg") String addMsg,
                           @Param("fromStatus") String fromStatus,
                           @Param("toStatus") String toStatus);
    public int updateStatus(@Param("id") int id,
                            @Param("status") String status,
                            @Param("addMsg") String addMsg);

    public List<Integer> retryMessageIds(@Param("pagesize") int pagesize,
                                         @Param("failStatus") String failStatus);

    public int retryStatusFresh(@Param("id") int id,
                                @Param("addMsg") String addMsg,
                                @Param("failStatus") String failStatus,
                                @Param("newStatus") String newStatus);

}
