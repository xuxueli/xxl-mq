package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.mapper.MessageMapper;
import com.xxl.mq.admin.model.adaptor.MessageAdaptor;
import com.xxl.mq.admin.model.dto.MessageDTO;
import com.xxl.mq.admin.model.entity.Message;
import com.xxl.mq.admin.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	public PageModel<MessageDTO> pageList(int offset, int pagesize) {

		// page
		List<Message> pageList = messageMapper.pageList(offset, pagesize);
		int totalCount = messageMapper.pageListCount(offset, pagesize);

		// adaptor
		List<MessageDTO> pageListForDTO = pageList.stream().map(MessageAdaptor::adaptor).collect(Collectors.toList());

		// result
		PageModel<MessageDTO> pageModel = new PageModel<>();
		pageModel.setPageData(pageListForDTO);
		pageModel.setTotalCount(totalCount);

		return pageModel;
	}

}
