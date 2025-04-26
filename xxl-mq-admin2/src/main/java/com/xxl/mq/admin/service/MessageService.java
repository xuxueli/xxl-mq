package com.xxl.mq.admin.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xxl.mq.admin.model.dto.MessageDTO;
import com.xxl.mq.admin.model.entity.Message;
import com.xxl.tool.response.Response;
import com.xxl.tool.response.PageModel;

/**
* Message Service
*
* Created by xuxueli on '2025-03-21 21:54:06'.
*/
public interface MessageService {

    /**
    * 新增
    */
    public Response<String> insert(MessageDTO message);

    /**
    * 删除
    */
    public Response<String> delete(List<Long> ids);

    /**
    * 更新
    */
    public Response<String> update(MessageDTO message);

    /**
    * Load查询
    */
    public Response<Message> load(int id);

    /**
    * 分页查询
    */
    public PageModel<MessageDTO> pageList(int offset, int pagesize, String topic, int status, Date effectTimeStart, Date effectTimeEnd);

    /**
     * 归档
     */
    public Response<String> archive(String topic, Integer archiveStrategy, int maxCycleCount);

    /**
     * 报表信息
     */
    public Response<Map<String, Object>> chartInfo(Date startDate, Date endDate);

    /**
     * 仪表盘信息
     */
    Map<String, Object> dashboardInfo();

}
