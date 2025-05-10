package com.xxl.mq.admin.service;

import java.util.Map;
import java.util.List;

import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.tool.response.Response;
import com.xxl.tool.response.PageModel;

/**
* Topic Service
*
* Created by xuxueli on '2025-03-21 12:52:25'.
*/
public interface TopicService {

    /**
    * 新增
    */
    public Response<String> insert(Topic topic);

    /**
    * 删除
    */
    public Response<String> delete(List<Integer> ids);

    /**
    * 更新
    */
    public Response<String> update(Topic topic);

    /**
    * Load查询
    */
    public Response<Topic> load(int id);

    /**
    * 分页查询
    */
    public PageModel<Topic> pageList(int offset, int pagesize, String appname, String topic);

    /**
     * 更新状态
     */
    public Response<String> updateStatus(List<Integer> ids, int status);

}
