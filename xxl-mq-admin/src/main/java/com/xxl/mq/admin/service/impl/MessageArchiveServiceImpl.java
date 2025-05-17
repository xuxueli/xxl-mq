package com.xxl.mq.admin.service.impl;

import com.xxl.mq.admin.constant.enums.ArchiveStrategyEnum;
import com.xxl.mq.admin.mapper.MessageArchiveMapper;
import com.xxl.mq.admin.mapper.TopicMapper;
import com.xxl.mq.admin.model.adaptor.MessageAdaptor;
import com.xxl.mq.admin.model.dto.MessageArchiveDTO;
import com.xxl.mq.admin.model.entity.MessageArchive;
import com.xxl.mq.admin.model.entity.Topic;
import com.xxl.mq.admin.service.MessageAichiveService;
import com.xxl.tool.core.DateTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.PageModel;
import com.xxl.tool.response.Response;
import com.xxl.tool.response.ResponseCode;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
* Message Service Impl
*
* Created by xuxueli on '2025-03-21 21:54:06'.
*/
@Service
public class MessageArchiveServiceImpl implements MessageAichiveService {

	@Resource
	private TopicMapper topicMapper;
	@Resource
	private MessageArchiveMapper messageArchiveMapper;


	/**
	* 分页查询
	*/
	@Override
	public PageModel<MessageArchiveDTO> pageList(int offset, int pagesize, String topic, Date effectTimeStart, Date effectTimeEnd) {
		PageModel<MessageArchiveDTO> pageModel = new PageModel<>();

		// valid
		if (StringTool.isBlank(topic)) {
			pageModel.setPageData(new ArrayList<>());
			pageModel.setTotalCount(0);
			return pageModel;
		}

		// page
		List<MessageArchive> pageList = messageArchiveMapper.pageList(offset, pagesize, topic, effectTimeStart, effectTimeEnd);
		int totalCount = messageArchiveMapper.pageListCount(offset, pagesize, topic, effectTimeStart, effectTimeEnd);

		// adaptor
		List<MessageArchiveDTO> pageListForDTO = pageList.stream().map(MessageAdaptor::adaptorToArchiveDto).collect(Collectors.toList());

		// result
		pageModel.setPageData(pageListForDTO);
		pageModel.setTotalCount(totalCount);

		return pageModel;
	}

	@Override
	public Response<String> clean(String topic, Integer archiveStrategy) {

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

		return Response.of(ResponseCode.CODE_200.getCode(), "操作成功，处理数据行数：" + cleanCount);
	}

	/**
     * clean and archive
	 *
	 * @param isArchive
     * @param effectTimeFrom
     * @return
	 */
	private long cleanAndArchive(String topic, boolean isArchive, Date effectTimeFrom){

		// 3、scroll clean archived data （old）
		int maxCycleCount = 1000;
		int pageSize = 100;
		int archeveNum = 0;
		int count = 1;
		while (maxCycleCount>0 && count > 0) {
			// next page
			count = messageArchiveMapper.batchClean(topic, isArchive, effectTimeFrom, pageSize);

			maxCycleCount--;
			archeveNum += count;
		}

		return archeveNum;
	}

}
