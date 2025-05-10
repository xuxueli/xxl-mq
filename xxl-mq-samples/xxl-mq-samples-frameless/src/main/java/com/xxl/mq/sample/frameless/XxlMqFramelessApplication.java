package com.xxl.mq.sample.frameless;


import com.xxl.mq.core.XxlMqHelper;
import com.xxl.mq.sample.frameless.conf.FramelessXxlMqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author xuxueli 2018-11-21
 */
public class XxlMqFramelessApplication {
    private static Logger logger = LoggerFactory.getLogger(XxlMqFramelessApplication.class);

    public static void main(String[] args) throws Exception {

        try {
            // start
            FramelessXxlMqConfig.getInstance().start();
            logger.info(">>>>>>>>>>> xxl-mq frameless started.");

            // produce
            for (int i = 0; i < 10; i++) {
                XxlMqHelper.produce("topic_sample", "data-" + i);
            }

            // Blocks until interrupted
            /*TimeUnit.SECONDS.sleep(5);*/
            while (true) {
                try {
                    TimeUnit.HOURS.sleep(1);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            // destroy
            FramelessXxlMqConfig.getInstance().stop();
            logger.info(">>>>>>>>>>> xxl-mq frameless stopped.");
        }

    }

}
