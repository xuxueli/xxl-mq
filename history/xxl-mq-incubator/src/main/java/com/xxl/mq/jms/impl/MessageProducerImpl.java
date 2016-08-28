package com.xxl.mq.jms.impl;

import com.xxl.mq.jms.Main;
import com.xxl.mq.util.JacksonUtil;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

public class MessageProducerImpl implements MessageProducer {

	@Override
	public void setDisableMessageID(boolean value) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getDisableMessageID() throws JMSException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDisableMessageTimestamp(boolean value) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getDisableMessageTimestamp() throws JMSException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setDeliveryMode(int deliveryMode) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getDeliveryMode() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPriority(int defaultPriority) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getPriority() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setTimeToLive(long timeToLive) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public long getTimeToLive() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Destination getDestination() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(Message message) throws JMSException {
		Main.queue.add(JacksonUtil.writeValueAsString(message));
		System.out.println("send msg:" + JacksonUtil.writeValueAsString(message));
	}

	@Override
	public void send(Message message, int deliveryMode, int priority,
			long timeToLive) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(Destination destination, Message message)
			throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(Destination destination, Message message,
			int deliveryMode, int priority, long timeToLive)
			throws JMSException {
		// TODO Auto-generated method stub

	}

}
