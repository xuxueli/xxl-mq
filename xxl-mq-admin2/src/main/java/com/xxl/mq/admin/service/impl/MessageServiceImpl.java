package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.mapper.MessageMapper;
import com.xxl.mq.admin.mapper.TopicMapper;
import com.xxl.mq.admin.model.adaptor.MessageAdaptor;
import com.xxl.mq.admin.model.dto.MessageDTO;
import com.xxl.mq.admin.model.entity.Message;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.service.MessageService;
import com.xxl.tool.core.StringTool;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import com.xxl.tool.response.Response;
import com.xxl.tool.response.ResponseBuilder;
import com.xxl.tool.response.PageModel;

/**
* Message Service Impl
*
* Created by xuxueli on '2025-03-21 21:54:06'.
*/
@Service
public class MessageServiceImpl implements MessageService {

	@Resource
	private MessageMapper messageMapper;
	@Resource
	private TopicMapper topicMapper;

	/**
    * 新增
    */
	@Override
	public Response<String> insert(MessageDTO messageDTO) {
		Message message = MessageAdaptor.adaptor(messageDTO);

		// valid
		if (message == null) {
			return new ResponseBuilder<String>().fail("必要参数缺失").build();
        }
		Topic topic = topicMapper.loadByTopic(messageDTO.getTopic());
		if (topic == null) {
			return new ResponseBuilder<String>().fail("参数非法：Topic").build();
		}


		messageMapper.insert(message);
		return new ResponseBuilder<String>().success().build();
	}

	/**
	* 删除
	*/
	@Override
	public Response<String> delete(List<Integer> ids) {
		int ret = messageMapper.delete(ids);
		return ret>0? new ResponseBuilder<String>().success().build()
					: new ResponseBuilder<String>().fail().build() ;
	}

	/**
	* 更新
	*/
	@Override
	public Response<String> update(MessageDTO messageDTO) {
		Message message = MessageAdaptor.adaptor(messageDTO);

		int ret = messageMapper.update(message);
		return ret>0? new ResponseBuilder<String>().success().build()
					: new ResponseBuilder<String>().fail().build() ;
	}

	/**
	* Load查询
	*/
	@Override
	public Response<Message> load(int id) {
		Message record = messageMapper.load(id);
		return new ResponseBuilder<Message>().success(record).build();
	}

	/**
	* 分页查询
	*/
	@Override
	public PageModel<MessageDTO> pageList(int offset, int pagesize, String topic, Date effectTimeStart, Date effectTimeEnd) {
		PageModel<MessageDTO> pageModel = new PageModel<>();

		// valid
		if (StringTool.isBlank(topic)) {
			pageModel.setPageData(new ArrayList<>());
			pageModel.setTotalCount(0);
			return pageModel;
		}

		// page
		List<Message> pageList = messageMapper.pageList(offset, pagesize, topic, effectTimeStart, effectTimeEnd);
		int totalCount = messageMapper.pageListCount(offset, pagesize, topic, effectTimeStart, effectTimeEnd);

		// adaptor
		List<MessageDTO> pageListForDTO = pageList.stream().map(MessageAdaptor::adaptor).collect(Collectors.toList());

		// result
		pageModel.setPageData(pageListForDTO);
		pageModel.setTotalCount(totalCount);

		return pageModel;
	}

}
