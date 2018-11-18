package com.xxl.mq.client.consumer;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface IMqConsumer {

    /**
     * consume message
     *
     * @param data
     * @return
     * @throws Exception
     */
    public MqResult consume(String data) throws Exception;

}
