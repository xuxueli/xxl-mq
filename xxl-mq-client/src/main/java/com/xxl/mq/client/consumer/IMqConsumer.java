package com.xxl.mq.client.consumer;

import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public interface IMqConsumer {

    public MqResult consume(Map<String, String> dataMap) throws Exception;

}
