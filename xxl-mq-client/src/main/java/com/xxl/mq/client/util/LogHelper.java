package com.xxl.mq.client.util;

import java.text.MessageFormat;

/**
 * @author xuxueli 2018-11-21
 */
public class LogHelper {


    private static final String LOG_TEMPLATE = "<hr>时间：{0}<br>操作：{1}<br>备注：{2}";

    /**
     * make log
     */
    public static String makeLog(String logTitle, String logContent){
        String log = MessageFormat.format(LOG_TEMPLATE, DateFormatUtil.getNowTime(), logTitle, logContent);
        return log;
    }

}
