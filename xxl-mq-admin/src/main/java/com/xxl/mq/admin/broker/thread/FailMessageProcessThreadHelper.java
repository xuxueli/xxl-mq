package com.xxl.mq.admin.broker.thread;

import com.xxl.mq.admin.broker.config.BrokerBootstrap;
import com.xxl.mq.admin.constant.enums.MessageStatusEnum;
import com.xxl.mq.admin.model.entity.Message;
import com.xxl.tool.concurrent.CyclicThread;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.DateTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * prude message queue helper
 *
 * @author xuxueli
 */
public class FailMessageProcessThreadHelper {
    private static final Logger logger = LoggerFactory.getLogger(FailMessageProcessThreadHelper.class);

    // ---------------------- init ----------------------

    private final BrokerBootstrap brokerBootstrap;
    public FailMessageProcessThreadHelper(BrokerBootstrap brokerBootstrap) {
        this.brokerBootstrap = brokerBootstrap;
    }

    // ---------------------- start / stop ----------------------

    /**
     * fail process interval, 60s
     */
    public static final int FAIL_PROCESS_TIME_INTERVAL = 3 * 60 * 1000;

    private volatile CyclicThread failMessageProcessThread;

    /**
     * start
     *
     * remark：
     *      1、FailMessage Process: 1、find stuck message(running >5min), mark fail；2、find fail-message and retry.
     */
    public void start(){

        failMessageProcessThread = new CyclicThread("failMessageProcessThread", true, new Runnable() {
            @Override
            public void run() {

                /**
                 * 1、find running-timeout（ running > 5min ） message, mark fail
                 */
                int updateStuck2FailCount = 0;
                int ret = brokerBootstrap.getMessageMapper().updateRunningTimeout2Fail(
                        MessageStatusEnum.RUNNING.getValue(),
                        DateTool.addMinutes(new Date(), -5),
                        MessageStatusEnum.EXECUTE_TIMEOUT.getValue(),
                        500);
                while (ret > 0) {
                    updateStuck2FailCount += ret;
                    // avoid too fast
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    // next page
                    ret = brokerBootstrap.getMessageMapper().updateRunningTimeout2Fail(
                            MessageStatusEnum.RUNNING.getValue(),
                            DateTool.addMinutes(new Date(), -5),
                            MessageStatusEnum.EXECUTE_TIMEOUT.getValue(),
                            50);
                }
                if (updateStuck2FailCount > 0) {
                    logger.info(">>>>>>>>>>> failMessageProcessThread, updateStuck2FailCount: {}", updateStuck2FailCount);
                }

                // 2、query fail-message, and retry
                int failRetryCount = 0;
                List<Integer> failStatusList = Arrays.asList(MessageStatusEnum.EXECUTE_FAIL.getValue(), MessageStatusEnum.EXECUTE_TIMEOUT.getValue());
                List<Message> retryFailData = brokerBootstrap.getMessageMapper().queryRetryDataByPage(failStatusList, 50);
                while (CollectionTool.isNotEmpty(retryFailData)) {
                    failRetryCount += retryFailData.size();
                    // do retry
                    brokerBootstrap.getMessageProduceAndConsumeThreadHelper().failRetry(retryFailData, failStatusList);

                    // avoid too fast
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    // next page
                    retryFailData = brokerBootstrap.getMessageMapper().queryRetryDataByPage(failStatusList, 50);
                }
                if (failRetryCount > 0) {
                    logger.info(">>>>>>>>>>> failMessageProcessThread, failRetryCount: {}", failRetryCount);
                }

            }
        }, FAIL_PROCESS_TIME_INTERVAL, true);
        failMessageProcessThread.start();

    }

    public void stop(){
        // do nothing
    }

}
