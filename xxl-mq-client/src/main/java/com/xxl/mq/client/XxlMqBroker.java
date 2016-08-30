package com.xxl.mq.client;

import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.netcom.NetComServerFactory;
import com.xxl.mq.client.service.XxlMqService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqBroker implements XxlMqService {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqBroker.class);

    // ---------------------- broker config ----------------------
    private static int port = 6080;
    private static XxlMqService xxlMqService;
    public void setPort(int port) {
        this.port = port;
    }
    public void setXxlMqService(XxlMqService xxlMqService) {
        XxlMqBroker.xxlMqService = xxlMqService;
    }

    // ---------------------- broker init ----------------------
    public void init() throws Exception {
        Map<String, Object> serviceMap = new HashMap<String, Object>();
        serviceMap.put(XxlMqService.class.getName(), this);
        new NetComServerFactory(port, serviceMap);
    }

    // ---------------------- broker proxy ----------------------
    private static LinkedBlockingQueue<Message> xxlMqMessageLinkedBlockingQueue = new LinkedBlockingQueue<Message>();
    private static Executor executor = Executors.newCachedThreadPool();
    static {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Message msg = xxlMqMessageLinkedBlockingQueue.take();
                        xxlMqService.saveMessage(msg);
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    }
                }
            }
        });
    }

    @Override
    public void saveMessage(Message message) {
        xxlMqMessageLinkedBlockingQueue.add(message);
    }

    @Override
    public LinkedList<Message> pageList(int pagesize, String name) {
        return xxlMqService.pageList(0, name);
    }
}
