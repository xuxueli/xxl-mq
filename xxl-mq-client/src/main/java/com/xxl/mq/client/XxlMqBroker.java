package com.xxl.mq.client;

import com.xxl.mq.client.rpc.netcom.NetComServerFactory;
import com.xxl.mq.client.service.BrokerService;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqBroker implements InitializingBean {

    // ---------------------- server config ----------------------
    private static int port = 6080;
    private BrokerService brokerService;
    public void setPort(int port) {
        this.port = port;
    }
    public void setBrokerService(BrokerService brokerService) {
        this.brokerService = brokerService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> serviceMap = new HashMap<String, Object>();
        serviceMap.put(BrokerService.class.getName(), brokerService);
        new NetComServerFactory(port, serviceMap);
    }

}
