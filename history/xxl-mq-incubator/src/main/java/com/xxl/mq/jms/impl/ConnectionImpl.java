package com.xxl.mq.jms.impl;

public class ConnectionImpl implements Connection {

	@Override
	public Session createSession(boolean transacted, int acknowledgeMode)
			throws JMSException {
		return new SessionImpl();
	}

	@Override
	public String getClientID() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClientID(String clientID) throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public ConnectionMetaData getMetaData() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ExceptionListener getExceptionListener() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExceptionListener(ExceptionListener listener)
			throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() throws JMSException {
		// TODO Auto-generated method stub

	}

	@Override
	public ConnectionConsumer createConnectionConsumer(Destination destination,
			String messageSelector, ServerSessionPool sessionPool,
			int maxMessages) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ConnectionConsumer createDurableConnectionConsumer(Topic topic,
			String subscriptionName, String messageSelector,
			ServerSessionPool sessionPool, int maxMessages) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}

}
