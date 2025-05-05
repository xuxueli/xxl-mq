package com.xxl.mq.core.context;

/**
 * xxl-mq context
 *
 * @author xuxueli 2025
 */
public class XxlMqContext {

    private String data;
    private int status;;
    private String consumeLog;

    public XxlMqContext() {
        status = 2;
        consumeLog = "";
    }
    public XxlMqContext(String data) {
        this.data = data;
        status = 2;
        consumeLog = "";
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getConsumeLog() {
        return consumeLog;
    }

    public void setConsumeLog(String consumeLog) {
        this.consumeLog = consumeLog;
    }

    // ---------------------- tool ----------------------

    private static final InheritableThreadLocal<XxlMqContext> contextHolder = new InheritableThreadLocal<>(); // support for child thread

    public static void setContext(XxlMqContext xxlMqContext){
        contextHolder.set(xxlMqContext);
    }

    public static XxlMqContext getContext(){
        return contextHolder.get();
    }

}