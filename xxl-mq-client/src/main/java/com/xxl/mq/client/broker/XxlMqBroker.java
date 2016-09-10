package com.xxl.mq.client.broker;

import com.xxl.mq.client.broker.biz.MqBrokerService;
import com.xxl.mq.client.message.Message;
import com.xxl.mq.client.rpc.netcom.NetComServerFactory;
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
public class XxlMqBroker implements MqBrokerService {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqBroker.class);

    // ---------------------- broker config ----------------------
    private static int port = 6080;
    private static MqBrokerService xxlMqService;
    public void setPort(int port) {
        this.port = port;
    }
    public void setXxlMqService(MqBrokerService xxlMqService) {
        XxlMqBroker.xxlMqService = xxlMqService;
    }

    // ---------------------- broker init ----------------------
    public void init() throws Exception {
        Map<String, Object> serviceMap = new HashMap<String, Object>();
        serviceMap.put(MqBrokerService.class.getName(), this);
        new NetComServerFactory(port, serviceMap);
    }
    public void destroy(){
    }

    // ---------------------- broker proxy ----------------------
    private static LinkedBlockingQueue<Message> newMessageQueue = new LinkedBlockingQueue<Message>();
    private static LinkedBlockingQueue<Message> consumeCallbackMessageQueue = new LinkedBlockingQueue<Message>();
    private static Executor executor = Executors.newCachedThreadPool();
    static {
        /**
         * async save message
         */
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Message msg = newMessageQueue.take();
                        xxlMqService.saveMessage(msg);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        });
        /**
         * async consume message callback process
         */
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Message msg = consumeCallbackMessageQueue.take();
                        xxlMqService.consumeCallbackMessage(msg);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        });

        /**
         * 重试消息处理
         */
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {

                }
            }
        });

    }

    @Override
    public int saveMessage(Message message) {
        return newMessageQueue.add(message)?1:-1;
    }

    @Override
    public LinkedList<Message> pullNewMessage(String name, int pagesize, int consumerRank, int consumerTotal) {
        return xxlMqService.pullNewMessage(name, pagesize, consumerRank, consumerTotal);
    }

    @Override
    public int lockMessage(int id, String addMsg) {
        return xxlMqService.lockMessage(id, addMsg);
    }

    public int consumeCallbackMessage(Message message) {
        return consumeCallbackMessageQueue.add(message)?1:-1;
    }

}
