package com.xxl.mq.jms;

import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.naming.NamingException;

import com.xxl.mq.jms.impl.ConnectionFactoryImpl;
import com.xxl.mq.jms.impl.ConnectionImpl;
import com.xxl.mq.jms.impl.MessageConsumerImpl;
import com.xxl.mq.jms.impl.MessageListenerImpl;
import com.xxl.mq.jms.impl.MessageProducerImpl;
import com.xxl.mq.jms.impl.ObjectMessageImpl;
import com.xxl.mq.jms.impl.SessionImpl;
import com.xxl.mq.jms.impl.TopicImpl;

public class Main {
	
	public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	
	public static void main(String[] args) throws NamingException, JMSException, InterruptedException {
		// 创建connection
		ConnectionFactoryImpl connectionFactory = new ConnectionFactoryImpl();
		ConnectionImpl connection = (ConnectionImpl) connectionFactory.createConnection();
		
		// 创建session
		SessionImpl session = (SessionImpl) connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		
		// topic
		Destination destination = new TopicImpl("topic_01");
		
		
		
		// producer
		MessageProducerImpl producer = (MessageProducerImpl) session.createProducer(destination);
		for (int i = 0; i < 10; i++) {
			// message
			ObjectMessageImpl message = (ObjectMessageImpl) session.createObjectMessage();
			message.setStringProperty("key" + i, "value" + i);
			
			producer.send(message);
		}
		
		// consumer
		MessageConsumerImpl consumer = (MessageConsumerImpl) session.createConsumer(destination);
		
		// listener
		MessageListener listener = new MessageListenerImpl(); 
		consumer.setMessageListener(listener);
				
		
		
	}
}
