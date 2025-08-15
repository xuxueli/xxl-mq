package com.xxl.mq.admin.mapper;

import com.xxl.mq.admin.model.entity.User;
import com.xxl.mq.core.constant.MessageStatusEnum;
import com.xxl.tool.core.DateTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessageMapperTest {
    private static Logger logger = LoggerFactory.getLogger(MessageMapperTest.class);

    @Resource
    private MessageMapper messageMapper;
    @Resource
    private MessageArchiveMapper messageArchiveMapper;

    @Test
    public void loadTest() throws Exception {

        int ARCHIVE_INTERVAL = 10 * 60 * 1000;

        Date failFrom = DateTool.addMilliseconds(new Date(), -1 * ARCHIVE_INTERVAL);
        Date failTo = new Date();
        List<Integer> failStatusList = Arrays.asList(MessageStatusEnum.EXECUTE_FAIL.getValue(), MessageStatusEnum.EXECUTE_TIMEOUT.getValue());

        int failCount = messageMapper.queryFailCount("topic_sample", failStatusList, failFrom, failTo);
        logger.info("failCount: {}", failCount);
        failCount = messageArchiveMapper.queryFailCount("topic_sample", failStatusList, failFrom, failTo);
        logger.info("failCount2: {}", failCount);
    }

}
