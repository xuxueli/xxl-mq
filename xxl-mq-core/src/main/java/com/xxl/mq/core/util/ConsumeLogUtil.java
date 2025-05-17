package com.xxl.mq.core.util;

import com.xxl.tool.core.DateTool;

import java.util.Date;

public class ConsumeLogUtil {

    public static final String HR_TAG = "<hr>";
    public static final String BR_TAG = "<br>";

    /**
     * append ConsumeLog
     *
     * @param originConsumeLog
     * @param logTitle
     * @param logContent
     */
    public static String appendConsumeLog(String originConsumeLog, String logTitle, String logContent){
        String consumeLog = originConsumeLog!=null ? originConsumeLog + "<hr>" : "";
        consumeLog += generateConsumeLog(logTitle, logContent);
        return consumeLog;
    }

    /**
     * generate ConsumeLog
     *
     * @param logTitle
     * @param logContent
     * @return
     */
    public static String generateConsumeLog(String logTitle, String logContent){
        String consumeLog = "【" + logTitle + "】" +
                "<br>时间：" + DateTool.formatDateTime(new Date());
        if (logContent != null) {
            consumeLog += "<br>备注：" + logContent;
        }

        return consumeLog;
    }

}
