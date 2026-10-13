package com.xxl.mq.admin.business.broker.thread;

import com.xxl.mq.admin.business.broker.config.BrokerBootstrap;
import com.xxl.mq.admin.business.mapper.MessageMapper;
import com.xxl.mq.admin.business.model.entity.Topic;
import com.xxl.mq.admin.business.util.PartitionUtil;
import com.xxl.mq.core.constant.MessageStatusEnum;
import com.xxl.mq.core.openapi.model.PullRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MessageProduceAndConsumeThreadHelperTest {

    @Test
    void pullAndLockShouldOnlyQueryTopicsBelongingToRequestingApplication() {
        BrokerBootstrap brokerBootstrap = mock(BrokerBootstrap.class);
        LocalCacheThreadHelper localCacheThreadHelper = mock(LocalCacheThreadHelper.class);
        MessageMapper messageMapper = mock(MessageMapper.class);
        when(brokerBootstrap.getLocalCacheThreadHelper()).thenReturn(localCacheThreadHelper);
        when(brokerBootstrap.getMessageMapper()).thenReturn(messageMapper);

        Topic ownedTopic = new Topic();
        ownedTopic.setAppname("app-a");
        Topic foreignTopic = new Topic();
        foreignTopic.setAppname("app-b");
        when(localCacheThreadHelper.findTopic("topic-owned")).thenReturn(ownedTopic);
        when(localCacheThreadHelper.findTopic("topic-foreign")).thenReturn(foreignTopic);
        when(localCacheThreadHelper.findPartitionRangeByAppnameAndUuid("app-a", "instance-a"))
                .thenReturn(new PartitionUtil.PartitionRange(1, 100));
        when(messageMapper.pullQuery(any(), eq(MessageStatusEnum.NEW.getValue()), eq(1), eq(100), any(Date.class), eq(10)))
                .thenReturn(Collections.emptyList());

        PullRequest pullRequest = new PullRequest();
        pullRequest.setAppname("app-a");
        pullRequest.setInstanceUuid("instance-a");
        pullRequest.setTopicList(List.of("topic-owned", "topic-foreign"));
        pullRequest.setBatchsize(10);

        new MessageProduceAndConsumeThreadHelper(brokerBootstrap).pullAndLock(pullRequest);

        verify(messageMapper).pullQuery(
                eq(List.of("topic-owned")),
                eq(MessageStatusEnum.NEW.getValue()),
                eq(1),
                eq(100),
                any(Date.class),
                eq(10));
    }
}
