package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.netcom.NetComClientProxy;
import com.xxl.mq.client.service.BrokerService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqProducer {

    private static BrokerService tcpService;
    public static BrokerService getInstance() throws Exception {
        if (tcpService == null) {
            tcpService = (BrokerService) new NetComClientProxy(BrokerService.class, 1000 * 5, null).getObject();
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

    public static void main(String[] args) {


        for (int i = 1; i <= 100; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("num", i+"");
            saveMessage(new Message("test", map));
        }

    }

}
