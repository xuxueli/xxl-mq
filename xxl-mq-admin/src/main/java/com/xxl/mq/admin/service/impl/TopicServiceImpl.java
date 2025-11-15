package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.constant.enums.TopicStatusEnum;
import com.xxl.mq.admin.mapper.TopicMapper;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.service.TopicService;
import com.xxl.tool.core.StringTool;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import com.xxl.tool.response.Response;
import com.xxl.tool.response.PageModel;

/**
* Topic Service Impl
*
* Created by xuxueli on '2025-03-21 12:52:25'.
*/
@Service
public class TopicServiceImpl implements TopicService {

	@Resource
	private TopicMapper topicMapper;

	/**
    * 新增
    */
	@Override
	public Response<String> insert(Topic topic) {

		// valid
		if (topic == null || StringTool.isBlank(topic.getTopic())) {
			return Response.ofFail("必要参数缺失");
        }
		topic.setTopic(topic.getTopic().trim());
		if (topic.getExecutionTimeout() > 10 * 60) {
			return Response.ofFail("超时时间不能超过10分钟");
		}

		// process
		Topic existTopic = topicMapper.loadByTopic(topic.getTopic());
		if (existTopic != null) {
			return Response.ofFail("Topic已存在");
		}

		topicMapper.insert(topic);
		return Response.ofSuccess();
	}

	/**
	* 删除
	*/
	@Override
	public Response<String> delete(List<Integer> ids) {
		int ret = topicMapper.delete(ids);
		return ret>0? Response.ofSuccess() : Response.ofFail();
	}

	/**
	* 更新
	*/
	@Override
	public Response<String> update(Topic topic) {

		// valid
		if (topic.getExecutionTimeout() > 10 * 60) {
			return Response.ofFail("超时时间不能超过10分钟");
		}

		// process
		int ret = topicMapper.update(topic);
		return ret>0? Response.ofSuccess() : Response.ofFail();
	}

	/**
	* Load查询
	*/
	@Override
	public Response<Topic> load(int id) {
		Topic record = topicMapper.load(id);
		return Response.ofSuccess(record);
	}

	/**
	* 分页查询
	*/
	@Override
	public PageModel<Topic> pageList(int offset, int pagesize, String appname, String topic) {
		PageModel<Topic> pageModel = new PageModel<Topic>();

		// valid
		if (StringTool.isBlank(appname)) {
			pageModel.setData(new ArrayList<>());
			pageModel.setTotal(0);
			return pageModel;
		}

		// query
		List<Topic> pageList = topicMapper.pageList(offset, pagesize, appname, topic);
		int totalCount = topicMapper.pageListCount(offset, pagesize, appname, topic);

		pageModel.setData(pageList);
		pageModel.setTotal(totalCount);

		return pageModel;
	}

	@Override
	public Response<String> updateStatus(List<Integer> ids, int status) {
		if (TopicStatusEnum.match(status, null) == null) {
			return Response.ofFail("参数非法");
		}
		int ret = topicMapper.updateStatus(ids, status);
		return Response.ofSuccess();
	}

}
