package com.xxl.mq.admin.business.service.impl;

import com.xxl.mq.admin.business.mapper.MessageArchiveMapper;
import com.xxl.mq.admin.business.mapper.MessageMapper;
import com.xxl.mq.admin.business.model.adaptor.MessageAdaptor;
import com.xxl.mq.admin.business.model.entity.Message;
import com.xxl.mq.admin.business.model.entity.MessageArchive;
import com.xxl.mq.core.constant.MessageStatusEnum;
import com.xxl.tool.core.CollectionTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Executes a single message archive batch.
 *
 * <p>The transaction is deliberately scoped to one small batch instead of the
 * complete archive job. This keeps lock and undo-log usage bounded while still
 * guaranteeing that live messages are not deleted when the archive insert
 * fails.</p>
 */
@Service
public class MessageArchiveBatchService {

    private final MessageMapper messageMapper;
    private final MessageArchiveMapper messageArchiveMapper;

    public MessageArchiveBatchService(MessageMapper messageMapper,
                                      MessageArchiveMapper messageArchiveMapper) {
        this.messageMapper = messageMapper;
        this.messageArchiveMapper = messageArchiveMapper;
    }

    /**
     * Archives and removes at most {@code pageSize} finished messages.
     *
     * <p>Archive rows must be written before live rows are removed. Both writes
     * share the same transaction, so any exception rolls back the whole batch
     * and leaves the live messages available for the next archive attempt.</p>
     *
     * @param topic topic to process
     * @param isArchive whether retained messages should be copied to the archive table
     * @param effectTimeFrom lower retention boundary; only used when {@code isArchive} is true
     * @param pageSize maximum number of messages in this transaction
     * @return number of live messages processed; {@code 0} when no data remains
     */
    @Transactional(rollbackFor = Exception.class)
    public int archiveBatch(String topic, boolean isArchive, Date effectTimeFrom, int pageSize) {
        List<Integer> archiveStatusList = Arrays.asList(
                MessageStatusEnum.EXECUTE_SUCCESS.getValue(),
                MessageStatusEnum.EXECUTE_FAIL.getValue(),
                MessageStatusEnum.EXECUTE_TIMEOUT.getValue());

        List<Message> messageList = messageMapper.queryFinishedData(
                topic,
                archiveStatusList,
                MessageStatusEnum.EXECUTE_SUCCESS.getValue(),
                pageSize);
        if (CollectionTool.isEmpty(messageList)) {
            return 0;
        }

        // Persist retained messages first. A failure here prevents deletion from the live table.
        if (isArchive) {
            List<MessageArchive> messageArchiveList = messageList.stream()
                    .filter(message -> message.getEffectTime().after(effectTimeFrom))
                    .map(MessageAdaptor::adaptorToArchive)
                    .collect(Collectors.toList());
            if (CollectionTool.isNotEmpty(messageArchiveList)) {
                messageArchiveMapper.batchInsert(messageArchiveList);
            }
        }

        // This delete participates in the same transaction as the archive insert above.
        List<Long> messageIdList = messageList.stream()
                .map(Message::getId)
                .collect(Collectors.toList());
        messageMapper.delete(messageIdList);

        return messageList.size();
    }
}
