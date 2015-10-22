package com.xxl.mq.client;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.core.model.QueueMessage;
import com.xxl.core.model.TopicMessage;
import com.xxl.mq.destination.Destination;
import com.xxl.mq.destination.impl.Topic;
import com.xxl.mq.factory.ConnectionFactory;
import com.xxl.mq.spring.IMessageService;
import com.xxl.mq.spring.MessageListener;

public class MessageConsumer {
	private static Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
	
	private ConnectionFactory connectionFactory;
	private Destination destination;
	private MessageListener messageListener;
	
	private ExecutorService consumer_threads;
	
	public void init() {
		if (destination !=null && messageListener != null) {
			consumer_threads = Executors.newCachedThreadPool();
			
			int thread_size = 0;
			if (destination instanceof Topic) {
				thread_size = 1;
			} else if(destination instanceof Queue) {
				thread_size = 3;
			}
			
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					while (true) {
						if (getDestination() instanceof Topic) {
							runTopic();
						} else if (getDestination() instanceof Queue) {
							runQueue();
						}
					}
				}
				
				public void runTopic(){
					Topic topic = (Topic) getDestination();
					IMessageService service = connectionFactory.getMessageService();
					
					List<TopicMessage> list = service.selectListTopic(50);
					if (list != null && list.size() > 0) {
						for (TopicMessage message : list) {
							getMessageListener().onMessage(message);
						}
					} else {
						try {
							TimeUnit.SECONDS.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				public void runQueue(){
					Queue queue = (Queue) getDestination();
					IMessageService service = connectionFactory.getMessageService();
					List<QueueMessage> list = service.selectListQueue(50);
					if (list != null && list.size() > 0) {
						for (QueueMessage message : list) {
							getMessageListener().onMessage(message);
						}
					} else {
						try {
							TimeUnit.SECONDS.sleep(5);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			};
			
			for (int i = 0; i < thread_size; i++) {
				consumer_threads.execute(runnable);
			}
			
		}
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
	public MessageListener getMessageListener() {
		return messageListener;
	}
	public void setMessageListener(MessageListener messageListener) {
		this.messageListener = messageListener;
	}
	
}
