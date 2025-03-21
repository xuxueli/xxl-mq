package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.constant.enums.TopicStatusEnum;
import com.xxl.mq.admin.mapper.TopicMapper;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.service.TopicService;
import com.xxl.tool.core.StringTool;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import com.xxl.tool.response.Response;
import com.xxl.tool.response.ResponseBuilder;
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
		if (topic == null) {
			return new ResponseBuilder<String>().fail("必要参数缺失").build();
        }

		Topic existTopic = topicMapper.loadByTopic(topic.getTopic());
		if (existTopic != null) {
			return new ResponseBuilder<String>().fail("Topic已存在").build();
		}

		topicMapper.insert(topic);
		return new ResponseBuilder<String>().success().build();
	}

	/**
	* 删除
	*/
	@Override
	public Response<String> delete(List<Integer> ids) {
		int ret = topicMapper.delete(ids);
		return ret>0? new ResponseBuilder<String>().success().build()
					: new ResponseBuilder<String>().fail().build() ;
	}

	/**
	* 更新
	*/
	@Override
	public Response<String> update(Topic topic) {
		int ret = topicMapper.update(topic);
		return ret>0? new ResponseBuilder<String>().success().build()
					: new ResponseBuilder<String>().fail().build() ;
	}

	/**
	* Load查询
	*/
	@Override
	public Response<Topic> load(int id) {
		Topic record = topicMapper.load(id);
		return new ResponseBuilder<Topic>().success(record).build();
	}

	/**
	* 分页查询
	*/
	@Override
	public PageModel<Topic> pageList(int offset, int pagesize, String appname, String topic) {
		PageModel<Topic> pageModel = new PageModel<Topic>();

		// valid
		if (StringTool.isBlank(appname)) {
			pageModel.setPageData(new ArrayList<>());
			pageModel.setTotalCount(0);
			return pageModel;
		}

		// query
		List<Topic> pageList = topicMapper.pageList(offset, pagesize, appname, topic);
		int totalCount = topicMapper.pageListCount(offset, pagesize, appname, topic);

		pageModel.setPageData(pageList);
		pageModel.setTotalCount(totalCount);

		return pageModel;
	}

	@Override
	public Response<String> updateStatus(List<Integer> ids, int status) {
		if (TopicStatusEnum.match(status, null) == null) {
			return new ResponseBuilder<String>().fail("参数非法").build();
		}
		int ret = topicMapper.updateStatus(ids, status);
		return new ResponseBuilder<String>().success().build();
	}

}
