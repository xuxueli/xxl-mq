package com.xxl.mq.jms.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import com.xxl.mq.jms.Main;
import com.xxl.mq.util.JacksonUtil;

public class MessageConsumerImpl implements MessageConsumer {
	
	private ExecutorService pool;
	
	public MessageConsumerImpl() {
		pool = Executors.newCachedThreadPool();
		Runnable run = new Runnable() {
			@Override
			public void run() {
				while (true) {
					System.out.println("1");
					MessageListener listener = null;
					try {
						listener = getMessageListener();
						if (listener == null) {
							continue;
						}
					} catch (JMSException e) {
						e.printStackTrace();
					}
					
					String oriObj = Main.queue.poll();
					if (oriObj != null) {
						ObjectMessageImpl message = JacksonUtil.readValue(oriObj, ObjectMessageImpl.class);
						listener.onMessage(message);
					} else {
						try {
							TimeUnit.SECONDS.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		
		for (int i = 0; i < 3; i++) {
			pool.execute(run);
		}
	}

	private MessageListener listener;
	
	@Override
	public String getMessageSelector() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageListener getMessageListener() throws JMSException {
		// TODO Auto-generated method stub
		return this.listener;
	}

	@Override
	public void setMessageListener(MessageListener listener)
			throws JMSException {
		this.listener = listener;

	}

	@Override
	public Message receive() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message receive(long timeout) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message receiveNoWait() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}

}
