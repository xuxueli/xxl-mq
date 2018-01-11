package com.xxl.mq.client.broker;

import com.xxl.mq.client.broker.remote.IXxlMqBroker;
import com.xxl.mq.client.broker.remote.IXxlMqMessageDao;
import com.xxl.mq.client.message.MessageStatus;
import com.xxl.mq.client.message.XxlMqMessage;
import com.xxl.mq.client.rpc.netcom.NetComServerFactory;
import com.xxl.mq.client.rpc.util.DateFormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuxueli on 16/8/28.
 */
public class XxlMqBroker implements IXxlMqBroker {
    private final static Logger logger = LoggerFactory.getLogger(XxlMqBroker.class);

    // ---------------------- broker config ----------------------
    private  static int port = 6080;
    private  static IXxlMqMessageDao xxlMqMessageDao;
    private  static int expiredDay=3;
    private  static boolean cleanExpiredData=true;

    public void setPort(int port) {
    	XxlMqBroker.port = port;
    }
    
    public void setXxlMqMessageDao(IXxlMqMessageDao xxlMqMessageDao) {
        XxlMqBroker.xxlMqMessageDao = xxlMqMessageDao;
    }

    public void setExpiredDay(int expiredDay) {
    	XxlMqBroker.expiredDay = expiredDay;
	}

	public void setCleanExpiredData(boolean cleanExpiredData) {
		XxlMqBroker.cleanExpiredData = cleanExpiredData;
	}

	// ---------------------- broker init ----------------------
    private static LinkedBlockingQueue<XxlMqMessage> newMessageQueue = new LinkedBlockingQueue<XxlMqMessage>();
    private static LinkedBlockingQueue<XxlMqMessage> consumeCallbackMessageQueue = new LinkedBlockingQueue<XxlMqMessage>();
    private static Executor executor = Executors.newCachedThreadPool();

    public void init() throws Exception {

        // init base broker biz
        /**
         * async save message
         */
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        XxlMqMessage msg = newMessageQueue.take();
                        xxlMqMessageDao.save(msg);
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
                        XxlMqMessage message = consumeCallbackMessageQueue.take();
                        xxlMqMessageDao.updateStatus(message.getId(), message.getStatus(), message.getMsg());
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
                int waitTim = 5;
                while (true) {
                    try {
                        List<Integer> msgIds = xxlMqMessageDao.retryMessageIds(100, MessageStatus.FAIL.name());
                        if (msgIds!=null && msgIds.size()>0) {
                            waitTim = 5;
                            for (int id: msgIds) {
                                String addMsg = MessageFormat.format("<hr>》》》时间: {0} <br>》》》操作: 失败消息触发重试,状态自动还原,剩余重试次数-1(status>>>NEW)", DateFormatUtil.formatDateTime(new Date()));
                                xxlMqMessageDao.retryStatusFresh(id, addMsg, MessageStatus.FAIL.name(), MessageStatus.NEW.name());
                            }
                        } else {
                            waitTim += 5;
                            if (waitTim>60) {
                                waitTim = 60;
                            }
                        }

                        TimeUnit.SECONDS.sleep(waitTim);
                    } catch (Exception e) {
                        logger.error("", e);
                    }
                }
            }
        });

        // registry broker rpc service
        Map<String, Object> serviceMap = new HashMap<String, Object>();
        serviceMap.put(IXxlMqBroker.class.getName(), this);
        new NetComServerFactory(port, serviceMap);

    }
    public void destroy(){
    }
    
    //----------------------clean expired data-----------------
    public void cleanExpiredData(){
		if (cleanExpiredData && expiredDay >= 1) {
			Date expiredDate=DateFormatUtil.getNowOrOffset(-1*expiredDay);
			xxlMqMessageDao.deleteExpiredMsg(expiredDate, MessageStatus.SUCCESS.name());
		}
    }

    // ---------------------- broker proxy ----------------------
    @Override
    public int saveMessage(XxlMqMessage message) {
        return newMessageQueue.add(message)?1:-1;
    }

    @Override
    public List<XxlMqMessage> pullNewMessage(String name, int pagesize, int consumerRank, int consumerTotal) {
        List<XxlMqMessage> list = xxlMqMessageDao.pullNewMessage(name, MessageStatus.NEW.name(), pagesize, consumerRank, consumerTotal);
        return list;
    }

    @Override
    public int lockMessage(int id, String addMsg) {
        return xxlMqMessageDao.lockMessage(id, addMsg, MessageStatus.NEW.name(), MessageStatus.ING.name());
    }

    public int consumeCallbackMessage(XxlMqMessage message) {
        return consumeCallbackMessageQueue.add(message)?1:-1;
    }

}
