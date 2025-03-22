package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.constant.enums.ArchiveStrategyEnum;
import com.xxl.mq.admin.constant.enums.MessageStatusEnum;
import com.xxl.mq.admin.mapper.MessageArchiveMapper;
import com.xxl.mq.admin.mapper.MessageMapper;
import com.xxl.mq.admin.mapper.TopicMapper;
import com.xxl.mq.admin.model.adaptor.MessageAdaptor;
import com.xxl.mq.admin.model.dto.MessageDTO;
import com.xxl.mq.admin.model.entity.Message;
import com.xxl.mq.admin.model.entity.MessageArchive;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.service.MessageService;
import com.xxl.mq.admin.util.ConsumeLogUtil;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.gson.GsonTool;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	@Resource
	private MessageArchiveMapper messageArchiveMapper;

	/**
    * 新增
    */
	@Override
	public Response<String> insert(MessageDTO messageDTO) {
		Message message = MessageAdaptor.adaptor(messageDTO);

		// valid
		if (message == null || StringTool.isBlank(messageDTO.getTopic())) {
			return new ResponseBuilder<String>().fail("必要参数缺失").build();
        }

		Topic topic = topicMapper.loadByTopic(messageDTO.getTopic());
		if (topic == null) {
			return new ResponseBuilder<String>().fail("参数非法：Topic").build();
		}

		// save
		message.setTopic(messageDTO.getTopic().trim());
		ConsumeLogUtil.appendConsumeLog(message, "人工新建消息", GsonTool.toJson(message));

		messageMapper.insert(message);
		return new ResponseBuilder<String>().success().build();
	}

	/**
	* 删除
	*/
	@Override
	public Response<String> delete(List<Long> ids) {
		int ret = messageMapper.delete(ids);
		return ret>0? new ResponseBuilder<String>().success().build()
					: new ResponseBuilder<String>().fail().build() ;
	}

	/**
	* 更新
	*/
	@Override
	public Response<String> update(MessageDTO messageDTO) {

		// valid
		Message message = messageMapper.load(messageDTO.getId());
		if (message == null) {
			return new ResponseBuilder<String>().fail("参数非法：消息ID").build();
		}

		// write
		message.setData(messageDTO.getData());
		message.setStatus(messageDTO.getStatus());
		message.setEffectTime(DateTool.parseDateTime(messageDTO.getEffectTime()));
		ConsumeLogUtil.appendConsumeLog(message, "人工修改消息", GsonTool.toJson(messageDTO));

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
	public PageModel<MessageDTO> pageList(int offset, int pagesize, String topic, int status, Date effectTimeStart, Date effectTimeEnd) {
		PageModel<MessageDTO> pageModel = new PageModel<>();

		// valid
		if (StringTool.isBlank(topic)) {
			pageModel.setPageData(new ArrayList<>());
			pageModel.setTotalCount(0);
			return pageModel;
		}

		// page
		List<Message> pageList = messageMapper.pageList(offset, pagesize, topic, status, effectTimeStart, effectTimeEnd);
		int totalCount = messageMapper.pageListCount(offset, pagesize, topic, status, effectTimeStart, effectTimeEnd);

		// adaptor
		List<MessageDTO> pageListForDTO = pageList.stream().map(MessageAdaptor::adaptor).collect(Collectors.toList());

		// result
		pageModel.setPageData(pageListForDTO);
		pageModel.setTotalCount(totalCount);

		return pageModel;
	}

	@Override
	public Response<String> archive(String topic, Integer archiveStrategy) {

		// valid
		Topic topicData = topicMapper.loadByTopic(topic);
		if (topicData == null) {
			return new ResponseBuilder<String>().fail("Topic非法").build();
		}
		ArchiveStrategyEnum archiveStrategyEnum = ArchiveStrategyEnum.match(archiveStrategy, null);
		if (archiveStrategyEnum == null) {
			return new ResponseBuilder<String>().fail("归档策略非法").build();
		}

		// archive
		long cleanCount = 0;
		switch (archiveStrategyEnum) {
			case RESERVE_7_DAY:
				cleanCount = cleanAndArchive(topic, true, DateTool.addDays(new Date(), -7));
				break;
			case RESERVE_30_DAY:
				cleanCount = cleanAndArchive(topic, true, DateTool.addDays(new Date(), -30));
				break;
			case RESERVE_90_DAY:
				cleanCount = cleanAndArchive(topic, true, DateTool.addDays(new Date(), -90));
				break;
			case RESERVE_FOREVER:
				cleanCount = cleanAndArchive(topic, true, DateTool.addDays(new Date(), -90));
				break;
			case NONE:
				cleanCount =cleanAndArchive(topic, false, null);
				break;
		}

		return new ResponseBuilder<String>().success("操作成功，处理数据行数：" + cleanCount).build();
	}

	/**
	 * clean and archive (TODO, need daily cycle cleaning )
	 *
	 * @param isArchive
	 * @param effectTimeFrom
	 * @return
	 */
	private long cleanAndArchive(String topic, boolean isArchive, Date effectTimeFrom){

		// init param
		List<Integer> archiveStatusList = Stream.of(MessageStatusEnum.EXECUTE_SUCCESS, MessageStatusEnum.EXECUTE_FAIL, MessageStatusEnum.EXECUTE_TIMEOUT)
				.map(MessageStatusEnum::getValue)
				.collect(Collectors.toList());

		int pageSize = 100;
		int maxCycleCount = 100; 	// Avoid dead loops

		List<Message> messageList = messageMapper.queryByStatus(topic, archiveStatusList, pageSize);
		long archeveNum = 0;
		while (maxCycleCount>0 && CollectionTool.isNotEmpty(messageList)){
			// 1、clean termination message
			List<Long> ids = messageList.stream().map(Message::getId).collect(Collectors.toList());
			messageMapper.delete(ids);
			archeveNum += ids.size();

			// 2、write to archive table （new）
			if (isArchive) {
				List<MessageArchive> messageArchiveList = messageList.stream()
						.filter(message -> message.getEffectTime().after(effectTimeFrom))
						.map(MessageAdaptor::adaptorToArchive)
						.collect(Collectors.toList());
				if (CollectionTool.isNotEmpty(messageArchiveList)) {
					messageArchiveMapper.batchInsert(messageArchiveList);
				}
			}

			// next page
			messageList = messageMapper.queryByStatus(topic, archiveStatusList, pageSize);
			maxCycleCount--;
		}

		// 3、scroll clean archived data （old）
		maxCycleCount = 100;
		int count = messageArchiveMapper.batchClean(topic, isArchive, effectTimeFrom, pageSize);
		while (maxCycleCount>0 && count > 0) {
			// next page
			count = messageArchiveMapper.batchClean(topic, isArchive, effectTimeFrom, pageSize);
			maxCycleCount--;
		}

		return archeveNum;
	}

}
