package com.xxl.mq.admin.model.adaptor;

import com.xxl.mq.admin.model.dto.MessageArchiveDTO;
import com.xxl.mq.admin.model.dto.MessageDTO;
import com.xxl.mq.admin.model.entity.Message;
import com.xxl.mq.admin.model.entity.MessageArchive;
import com.xxl.tool.core.DateTool;

public class MessageAdaptor {

    public static Message adaptor(MessageDTO messageDTO) {
        Message message = new Message();
        message.setId(messageDTO.getId());
        message.setTopic(messageDTO.getTopic());
        message.setPartitionId(messageDTO.getPartitionId());
        message.setData(messageDTO.getData());
        message.setStatus(messageDTO.getStatus());
        message.setEffectTime(DateTool.parseDateTime(messageDTO.getEffectTime()));
        message.setConsumeLog(messageDTO.getConsumeLog());
        message.setConsumeInstanceUuid(messageDTO.getConsumeInstanceUuid());
        message.setAddTime(messageDTO.getAddTime());
        message.setUpdateTime(messageDTO.getUpdateTime());
        return message;
    }

    public static MessageDTO adaptor(Message message) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setId(message.getId());
        messageDTO.setTopic(message.getTopic());
        messageDTO.setPartitionId(message.getPartitionId());
        messageDTO.setData(message.getData());
        messageDTO.setStatus(message.getStatus());
        messageDTO.setEffectTime(DateTool.formatDateTime(message.getEffectTime()));
        messageDTO.setConsumeLog(message.getConsumeLog());
        messageDTO.setConsumeInstanceUuid(message.getConsumeInstanceUuid());
        messageDTO.setAddTime(message.getAddTime());
        messageDTO.setUpdateTime(message.getUpdateTime());
        return messageDTO;
    }

    // --------------------------------- for archive ----------------------

    public static MessageArchive adaptorToArchive(Message message) {
        MessageArchive messageArchive = new MessageArchive();
        messageArchive.setId(message.getId());
        messageArchive.setTopic(message.getTopic());
        messageArchive.setPartitionId(message.getPartitionId());
        messageArchive.setData(message.getData());
        messageArchive.setStatus(message.getStatus());
        messageArchive.setEffectTime(message.getEffectTime());
        messageArchive.setConsumeLog(message.getConsumeLog());
        messageArchive.setConsumeInstanceUuid(message.getConsumeInstanceUuid());
        messageArchive.setAddTime(message.getAddTime());
        messageArchive.setUpdateTime(message.getUpdateTime());
        return messageArchive;
    }

    public static MessageArchiveDTO adaptorToArchiveDto(MessageArchive messageArchive) {
        MessageArchiveDTO messageArchiveDTO = new MessageArchiveDTO();
        messageArchiveDTO.setId(messageArchive.getId());
        messageArchiveDTO.setTopic(messageArchive.getTopic());
        messageArchiveDTO.setPartitionId(messageArchive.getPartitionId());
        messageArchiveDTO.setData(messageArchive.getData());
        messageArchiveDTO.setStatus(messageArchive.getStatus());
        messageArchiveDTO.setEffectTime(DateTool.formatDateTime(messageArchive.getEffectTime()));
        messageArchiveDTO.setConsumeLog(messageArchive.getConsumeLog());
        messageArchiveDTO.setConsumeInstanceUuid(messageArchive.getConsumeInstanceUuid());
        messageArchiveDTO.setAddTime(messageArchive.getAddTime());
        messageArchiveDTO.setUpdateTime(messageArchive.getUpdateTime());
        return messageArchiveDTO;
    }

}
