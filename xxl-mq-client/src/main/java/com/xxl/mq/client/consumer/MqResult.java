package com.xxl.mq.client.consumer;

/**
 * @author xuxueli 2018-11-17 21:31:01
 */
public class MqResult {

    // code
    public static final String SUCCESS_CODE = "SUCCESS";
    public static final String FAIL_CODE = "FAIL";   // default

    // result
    public static final MqResult SUCCESS = new MqResult(SUCCESS_CODE);
    public static final MqResult FAIL = new MqResult(FAIL_CODE);


    // field
    public String code;
    public String log;


    // construct
    public MqResult() {
    }
    public MqResult(String code) {
        this.code = code;
    }
    public MqResult(String code, String log) {
        this.code = code;
        this.log = log;
    }


    // set get
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }


    // tool
    public boolean isSuccess(){
        return SUCCESS_CODE.equalsIgnoreCase(code);
    }

}
