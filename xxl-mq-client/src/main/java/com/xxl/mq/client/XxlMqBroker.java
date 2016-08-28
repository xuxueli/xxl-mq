package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.netcom.NetComClientProxy;
import com.xxl.mq.client.rpc.netcom.NetComServerFactory;
import com.xxl.mq.client.service.BrokerService;
import com.xxl.mq.client.service.ConsumerHandler;
import com.xxl.mq.client.service.MessageManage;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqBroker implements InitializingBean {

    // ---------------------- server config ----------------------
    private static int port = 6080;
    private BrokerService brokerService;
    private static MessageManage messageManage;
    public void setPort(int port) {
        this.port = port;
    }
    public void setBrokerService(BrokerService brokerService) {
        this.brokerService = brokerService;
    }
    public void setMessageManage(MessageManage messageManage) {
        this.messageManage = messageManage;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> serviceMap = new HashMap<String, Object>();
        serviceMap.put(BrokerService.class.getName(), brokerService);
        new NetComServerFactory(port, serviceMap);
    }

    // ---------------------- process message ----------------------
    public static ConsumerHandler getInstance(String mqName) throws Exception {
        String registryKey = ConsumerHandler.class.getName().concat(":").concat(mqName);
        return (ConsumerHandler) new NetComClientProxy(ConsumerHandler.class, 1000 * 5, registryKey).getObject();
    }

    private static Executor executor = Executors.newCachedThreadPool();
    static {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        LinkedList<Message> list = messageManage.pageList(20, "test");
                        if (list!=null) {
                            for (Message message: list) {
                                getInstance(message.getName()).consume(message);

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

}
