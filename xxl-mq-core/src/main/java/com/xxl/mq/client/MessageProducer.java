package com.xxl.mq.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.core.model.QueueMessage;
import com.xxl.core.model.TopicMessage;
import com.xxl.mq.destination.Destination;
import com.xxl.mq.destination.impl.Queue;
import com.xxl.mq.destination.impl.Topic;
import com.xxl.mq.factory.ConnectionFactory;
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
	public void send(Object message) {
		if (destination instanceof Topic && message instanceof TopicMessage) {
			IMessageService service = this.connectionFactory.getMessageService();
			Topic topic = (Topic) destination;
			
			TopicMessage topicMessage = (TopicMessage) message;
			topicMessage.setTopicName(topic.getTopicName());
			
			// validate
			if (topic.getTopicName() == null 
					|| topic.getTopicName().trim().length() == 0
					|| topicMessage.getEffectTime() == null) {
				logger.info(">>>>>>>>>>> xxl-mq producer send topic msg fail, error params ,:[destination:{} ,message{}]", 
						JacksonUtil.writeValueAsString(destination), JacksonUtil.writeValueAsString(message));
				return;
			}
			
			service.addTopidMessage(topicMessage);
		} else if (destination instanceof Queue && message instanceof QueueMessage) {
			IMessageService service = this.connectionFactory.getMessageService();
			Queue queue = (Queue) destination;
			
			QueueMessage queueMessage = (QueueMessage) message;
			queueMessage.setQueueName(queue.getQueueName());
			
			// validate
			if (queue.getQueueName() == null 
					|| queue.getQueueName().trim().length() == 0 
					|| queueMessage.getEffectTime() == null) {
				logger.info(">>>>>>>>>>> xxl-mq producer send queue msg fail, error params ,:[destination:{} ,message{}]", 
						JacksonUtil.writeValueAsString(destination), JacksonUtil.writeValueAsString(message));
				return;
			}
			
			service.addQueueMessage(queueMessage);
		} else {
			throw new IllegalArgumentException("xxl-mq producer, send illegal message");
		}
		logger.info(">>>>>>>>>>> xxl-mq send msg success:[destination:{} ,message{}]", 
				JacksonUtil.writeValueAsString(destination), JacksonUtil.writeValueAsString(message));
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
