package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.constant.enums.ArchiveStrategyEnum;
import com.xxl.mq.admin.constant.enums.MessageStatusEnum;
import com.xxl.mq.admin.mapper.*;
import com.xxl.mq.admin.model.adaptor.MessageAdaptor;
import com.xxl.mq.admin.model.dto.MessageDTO;
import com.xxl.mq.admin.model.entity.*;
import com.xxl.mq.admin.service.MessageService;
import com.xxl.mq.core.util.ConsumeLogUtil;
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
	private MessageReportMapper messageReportMapper;
	@Resource
	private TopicMapper topicMapper;
	@Resource
	private ApplicationMapper applicationMapper;
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
			return Response.ofFail("必要参数缺失");
        }

		Topic topic = topicMapper.loadByTopic(messageDTO.getTopic());
		if (topic == null) {
			return Response.ofFail("参数非法：Topic");
		}
		message.setRetryCountRemain(topic.getRetryCount());

		// save
		message.setTopic(messageDTO.getTopic().trim());
		message.setConsumeLog(ConsumeLogUtil.appendConsumeLog(message.getConsumeLog(), "人工新建消息", GsonTool.toJson(message)));

		messageMapper.insert(message);
		return Response.ofSuccess();
	}

	/**
	* 删除
	*/
	@Override
	public Response<String> delete(List<Long> ids) {
		int ret = messageMapper.delete(ids);
		return ret>0? Response.ofSuccess(): Response.ofFail() ;
	}

	/**
	* 更新
	*/
	@Override
	public Response<String> update(MessageDTO messageDTO) {

		// valid
		Message message = messageMapper.load(messageDTO.getId());
		if (message == null) {
			return Response.ofFail("参数非法：消息ID（"+ messageDTO.getId() +"）");
		}

		// write
		message.setData(messageDTO.getData());
		message.setStatus(messageDTO.getStatus());
		message.setEffectTime(DateTool.parseDateTime(messageDTO.getEffectTime()));
		message.setConsumeLog(ConsumeLogUtil.appendConsumeLog(message.getConsumeLog(), "人工修改消息", GsonTool.toJson(messageDTO)));

		int ret = messageMapper.update(message);
		return ret>0? Response.ofSuccess() : Response.ofFail();
	}

	/**
	* Load查询
	*/
	@Override
	public Response<Message> load(int id) {
		Message record = messageMapper.load(id);
		return Response.ofSuccess(record);
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
	public Response<String> archive(String topic, Integer archiveStrategy, int maxCycleCount) {

		// valid
		Topic topicData = topicMapper.loadByTopic(topic);
		if (topicData == null) {
			return Response.ofFail("Topic非法");
		}
		ArchiveStrategyEnum archiveStrategyEnum = ArchiveStrategyEnum.match(archiveStrategy, null);
		if (archiveStrategyEnum == null) {
			return Response.ofFail("归档策略非法");
		}

		// archive
		long cleanCount = cleanAndArchive(topic, archiveStrategyEnum, maxCycleCount);
		return Response.ofSuccess("操作成功，处理数据行数：" + cleanCount);
	}

	/**
	 * clean and archive
	 */
	private long cleanAndArchive(String topic, ArchiveStrategyEnum archiveStrategyEnum, int maxCycleCount){

		// archive strategy param
		boolean isArchive = false;
		Date effectTimeFrom = new Date();
		switch (archiveStrategyEnum) {
			case RESERVE_7_DAY:
				isArchive = true;
				effectTimeFrom = DateTool.addDays(new Date(), -7);
				break;
			case RESERVE_30_DAY:
				isArchive = true;
				effectTimeFrom = DateTool.addDays(new Date(), -30);
				break;
			case RESERVE_90_DAY:
				isArchive = true;
				effectTimeFrom = DateTool.addDays(new Date(), -90);
				break;
			case RESERVE_FOREVER:
				isArchive = true;
				effectTimeFrom = DateTool.addDays(new Date(), -90);
				break;
			case NONE:
				isArchive = false;
				effectTimeFrom = null;
				break;
		}
		Date finalEffectTimeFrom = effectTimeFrom;

		// init param
		List<Integer> archiveStatusList = Stream.of(MessageStatusEnum.EXECUTE_SUCCESS, MessageStatusEnum.EXECUTE_FAIL, MessageStatusEnum.EXECUTE_TIMEOUT)
				.map(MessageStatusEnum::getValue)
				.collect(Collectors.toList());

		// do archive
		int pageSize = 100;
		long archiveNum = 0;
		List<Message> messageList = messageMapper.queryFinishedData(topic, archiveStatusList, pageSize);
		while (maxCycleCount>0 && CollectionTool.isNotEmpty(messageList)){			// maxCycleCount: Avoid dead loops

			// 1、clean finished message
			List<Long> ids = messageList.stream().map(Message::getId).collect(Collectors.toList());
			messageMapper.delete(ids);
			archiveNum += ids.size();

			// 2、write to archive table （new）
			if (isArchive) {
				List<MessageArchive> messageArchiveList = messageList.stream()
						.filter(message -> message.getEffectTime().after(finalEffectTimeFrom))
						.map(MessageAdaptor::adaptorToArchive)
						.collect(Collectors.toList());
				if (CollectionTool.isNotEmpty(messageArchiveList)) {
					messageArchiveMapper.batchInsert(messageArchiveList);
				}
			}

			// next page
			messageList = messageMapper.queryFinishedData(topic, archiveStatusList, pageSize);
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

		return archiveNum;
	}

	@Override
	public Response<Map<String, Object>> chartInfo(Date startDate, Date endDate) {

		// param
		List<String> dayList = new ArrayList<String>();
		List<Long> dayNewCountList = new ArrayList<>();
		List<Long> dayRunningCountList = new ArrayList<>();
		List<Long> daySuccessCountList = new ArrayList<>();
		List<Long> dayFailCountList = new ArrayList<>();
		int newCountTotal = 0;
		int runningTotal = 0;
		int successTotal = 0;
		int failTotal = 0;

		// process data
		List<MessageReport> dayReportList = messageReportMapper.queryReport(startDate, endDate);
		if (CollectionTool.isNotEmpty(dayReportList)) {
			for (MessageReport item: dayReportList) {
				dayList.add(DateTool.formatDate(item.getProduceDay()));
				dayNewCountList.add(item.getNewCount());
				dayRunningCountList.add(item.getRunningCount());
				daySuccessCountList.add(item.getSucCount());
				dayFailCountList.add(item.getFailCount());

				newCountTotal += item.getNewCount();
				runningTotal += item.getRunningCount();
				successTotal += item.getSucCount();
				failTotal += item.getFailCount();
			}
		} else {
			for (int i = -6; i <= 0; i++) {
				dayList.add(DateTool.formatDate(DateTool.addDays(new Date(), i)));
				dayNewCountList.add(0L);
				dayRunningCountList.add(0L);
				daySuccessCountList.add(0L);
				dayFailCountList.add(0L);
			}
		}

		// result
		Map<String, Object> result = new HashMap<>();
		result.put("dayList", dayList);
		result.put("dayNewCountList", dayNewCountList);
		result.put("dayRunningCountList", dayRunningCountList);
		result.put("daySuccessCountList", daySuccessCountList);
		result.put("dayFailCountList", dayFailCountList);

		result.put("newCountTotal", newCountTotal);
		result.put("runningTotal", runningTotal);
		result.put("successTotal", successTotal);
		result.put("failTotal", failTotal);

		return Response.ofSuccess(result);
	}

	@Override
	public Map<String, Object> dashboardInfo() {

		// load data
		List<Application> applicationList = applicationMapper.findAll();
		int topicCount = topicMapper.count();

		// load messageCount(1 years)
		long messageCount = 0;
		Date endDate = DateTool.setStartOfDay(DateTool.addDays(new Date(), 1));
		Date startDate = DateTool.addYears(endDate, -1);
		List<MessageReport> dayReportList = messageReportMapper.queryReport(startDate, endDate);
		if (CollectionTool.isNotEmpty(dayReportList)) {
			for (MessageReport item: dayReportList) {
				messageCount += item.getTotalCount();
			}
		}

		// result
		Map<String, Object> result = new HashMap<>();
		result.put("applicationCount", applicationList.size());
		result.put("topicCount", topicCount);
		result.put("messageCount", messageCount);

		return result;
	}

}
