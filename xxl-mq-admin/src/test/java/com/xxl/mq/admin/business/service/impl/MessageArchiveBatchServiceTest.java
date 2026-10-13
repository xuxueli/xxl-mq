package com.xxl.mq.admin.business.service.impl;

import com.xxl.mq.admin.business.mapper.MessageArchiveMapper;
import com.xxl.mq.admin.business.mapper.MessageMapper;
import com.xxl.mq.admin.business.model.entity.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageArchiveBatchServiceTest {

    private MessageMapper messageMapper;
    private MessageArchiveMapper messageArchiveMapper;
    private MessageArchiveBatchService archiveBatchService;

    @BeforeEach
    void setUp() {
        messageMapper = mock(MessageMapper.class);
        messageArchiveMapper = mock(MessageArchiveMapper.class);
        archiveBatchService = new MessageArchiveBatchService(messageMapper, messageArchiveMapper);
    }

    @Test
    void shouldInsertArchiveBeforeDeletingLiveMessages() {
        Message message = newMessage(1L, new Date());
        when(messageMapper.queryFinishedData(eq("topic"), anyList(), eq(2), eq(100)))
                .thenReturn(List.of(message));

        int processedCount = archiveBatchService.archiveBatch(
                "topic", true, new Date(message.getEffectTime().getTime() - 1), 100);

        Assertions.assertEquals(1, processedCount);
        InOrder inOrder = inOrder(messageMapper, messageArchiveMapper);
        inOrder.verify(messageMapper).queryFinishedData(eq("topic"), anyList(), eq(2), eq(100));
        inOrder.verify(messageArchiveMapper).batchInsert(anyList());
        inOrder.verify(messageMapper).delete(List.of(1L));
    }

    @Test
    void shouldNotDeleteLiveMessagesWhenArchiveInsertFails() {
        Message message = newMessage(1L, new Date());
        when(messageMapper.queryFinishedData(eq("topic"), anyList(), eq(2), eq(100)))
                .thenReturn(List.of(message));
        doThrow(new IllegalStateException("archive insert failed"))
                .when(messageArchiveMapper).batchInsert(anyList());

        Assertions.assertThrows(
                IllegalStateException.class,
                () -> archiveBatchService.archiveBatch(
                        "topic", true, new Date(message.getEffectTime().getTime() - 1), 100));

        verify(messageMapper, never()).delete(anyList());
    }

    @Test
    void archiveBatchShouldDeclareTransactionRollback() throws Exception {
        Method method = MessageArchiveBatchService.class.getMethod(
                "archiveBatch", String.class, boolean.class, Date.class, int.class);
        Transactional transactional = method.getAnnotation(Transactional.class);

        Assertions.assertNotNull(transactional);
        Assertions.assertArrayEquals(new Class<?>[]{Exception.class}, transactional.rollbackFor());
    }

    private static Message newMessage(long id, Date effectTime) {
        Message message = new Message();
        message.setId(id);
        message.setTopic("topic");
        message.setEffectTime(effectTime);
        return message;
    }
}
