package com.xxl.mq.admin.service;

import com.xxl.mq.admin.model.dto.MessageArchiveDTO;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;

import java.util.Date;

/**
* Message Service
*
* Created by xuxueli on '2025-03-21 21:54:06'.
*/
public interface MessageAichiveService {

    /**
    * 分页查询
    */
    public PageModel<MessageArchiveDTO> pageList(int offset, int pagesize, String topic, int status, long bizId, Date effectTimeStart, Date effectTimeEnd);

    /**
     * 清理
     */
    public Response<String> clean(String topic, Integer archiveStrategy);

}
