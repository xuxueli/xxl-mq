package com.xxl.mq.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.mq.destination.Destination;
import com.xxl.mq.destination.impl.Queue;
import com.xxl.mq.destination.impl.Topic;
import com.xxl.mq.factory.ConnectionFactory;
import com.xxl.mq.message.impl.ObjectMessage;
import com.xxl.mq.spring.IMessageService;
import com.xxl.mq.util.JacksonUtil;

public class MessageProducer {
	private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);
	
	private ConnectionFactory connectionFactory;
	private Destination destination;
	
	/**
	 * send message
	 * @param message
	 */
	public void send(ObjectMessage message) {
		logger.debug("############# xxl-mq send msg:[destination:{},message{}]", 
				JacksonUtil.writeValueAsString(destination), JacksonUtil.writeValueAsString(message));
		if (destination instanceof Topic) {
			sendTopicMessage(message);
		} else if (destination instanceof Queue) {
			sendQueueMessage(message);
		}
	}
	
	/**
	 * send to topic
	 * @param message
	 */
	private void sendTopicMessage(ObjectMessage message){
		Topic topic = (Topic) destination;
		IMessageService service = this.connectionFactory.getMessageService();
		service.addTopidMessage(topic, message);
	}
	
	/**
	 * send to queue
	 * @param message
	 */
	private void sendQueueMessage(ObjectMessage message){
		Queue queue = (Queue) destination;
		IMessageService service = this.connectionFactory.getMessageService();
		service.addQueueMessage(queue, message);
	}

	public ConnectionFactory getConnectionFactory() {
		return connectionFactory;
	}
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	public Destination getDestination() {
		return destination;
	}
	public void setDestination(Destination destination) {
		this.destination = destination;
	}
	
}
