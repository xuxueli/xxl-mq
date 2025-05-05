package com.xxl.mq.core.consumer;

/**
 * xxl-mq consumer annotation
 *
 * Created by xuxueli on 16/8/28.
 */
public abstract class IConsumer {

    /**
     * consume message
     *
     * @throws Exception
     */
    public abstract void consume() throws Exception;

    /**
     * init method, invoked when IConsumer init
     */
    public void init() throws Exception {
        // do something
    }


    /**
     * destroy method, invoked when IConsumer destroy
     */
    public void destroy() throws Exception {
        // do something
    }

}