package com.xxl.mq.core.context;

/**
 * xxl-mq context
 *
 * @author xuxueli 2025
 */
public class XxlMqContext {

    /**
     * message id
     */
    private long id;

    /**
     * message data
     */
    private String data;

    /**
     * message status
     *      NEW(0, "未消费"),
     *      RUNNING(1, "消费中"),
     *      EXECUTE_SUCCESS(2, "消费成功"),
     *      EXECUTE_FAIL(3, "消费失败"),
     *      EXECUTE_TIMEOUT(4, "消费超时");
     */
    private int status;;

    /**
     * message consume log
     */
    private String consumeLog;

    public XxlMqContext() {
        status = 2;
        consumeLog = "";
    }
    public XxlMqContext(long id, String data) {
        this.id = id;
        this.data = data;
        status = 2;
        consumeLog = "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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