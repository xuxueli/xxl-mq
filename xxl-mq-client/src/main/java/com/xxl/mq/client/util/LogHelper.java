package com.xxl.mq.client.util;

import java.text.MessageFormat;
import java.util.Date;

/**
 * @author xuxueli 2018-11-21
 */
public class LogHelper {

    private static final String LOG_TEMPLATE = "<hr>时间：{0}<br>操作：{1}<br>备注：{2}";

    /**
     * make log
     */
    public static String makeLog(String logTitle, String logContent){

        String tim = DateUtil.formatDateTime(new Date());
        String log = MessageFormat.format(LOG_TEMPLATE, tim, logTitle, logContent);

        // sub
        if (log.length() > 20000) {
            log = log.substring(0, 20000) + "...";
        }

        return log;
    }

}
