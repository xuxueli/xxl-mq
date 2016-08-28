package com.xxl.mq.jms.impl;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

public class ConnectionFactoryImpl implements ConnectionFactory{

	@Override
	public Connection createConnection() throws JMSException {
		return new ConnectionImpl();
	}

	@Override
	public Connection createConnection(String userName, String password)
			throws JMSException {
		return new ConnectionImpl();
	}

}
