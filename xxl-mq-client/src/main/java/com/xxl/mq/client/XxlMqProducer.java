package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.netcom.NetComClientProxy;
import com.xxl.mq.client.service.BrokerService;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqProducer {

    private static BrokerService tcpService;
    public static BrokerService getInstance() throws Exception {
        if (tcpService == null) {
            tcpService = (BrokerService) new NetComClientProxy(BrokerService.class, 1000 * 5).getObject();
        }
        return tcpService;
    }

    public static void saveMessage(Message message){
        try {
            getInstance().saveMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
