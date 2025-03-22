package com.xxl.mq.admin.util;

import com.xxl.mq.admin.model.entity.Message;
import com.xxl.tool.core.DateTool;

import java.util.Date;

public class ConsumeLogUtil {

    /**
     * append ConsumeLog
     message
     * @param message
     * @param logTitle
     * @param logContent
     */
    public static void appendConsumeLog(Message message, String logTitle, String logContent){
        String consumeLog = message.getConsumeLog()!=null ? message.getConsumeLog() + "<hr>" : "";
        consumeLog += "【" + logTitle + "】" +
                "<br>时间：" + DateTool.formatDateTime(new Date());
        if (logContent != null) {
            consumeLog += "<br>备注：" + logContent;
        }

        message.setConsumeLog(consumeLog);
    }

}
