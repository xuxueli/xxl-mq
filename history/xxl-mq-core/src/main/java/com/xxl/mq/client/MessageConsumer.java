package com.xxl.mq.client;

import com.xxl.core.model.QueueLock;
import com.xxl.core.model.QueueMessage;
import com.xxl.core.model.QueueMessage.StatusEnum;
import com.xxl.core.model.TopicMessage;
import com.xxl.mq.destination.Destination;
import com.xxl.mq.destination.impl.Queue;
import com.xxl.mq.destination.impl.Topic;
import com.xxl.mq.factory.ConnectionFactory;
import com.xxl.mq.spring.IMessageService;
import com.xxl.mq.spring.MessageListener;
import com.xxl.mq.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * core comsumer
 * @author xuxueli 2015-10-26 14:10:57
 */
public class MessageConsumer {
	private static Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
	
	private ConnectionFactory connectionFactory;
	private Destination destination;
	private MessageListener messageListener;
	
	private ExecutorService consumer_threads;
	private boolean consumer_concurrent_switch = true;	// comsumer concurrent switch
	private int consumer_concurrent_num = 1;			// consumer concurrent num
	
	public void init() {
		if (destination ==null || messageListener == null) {
			logger.info("xxl-mq consumer init fail, destination|messageListener is null.");
			return;
		}

		consumer_threads = Executors.newCachedThreadPool();
		if (destination instanceof Topic) {
			// topic do not need concurrent switch
			for (int i = 0; i < consumer_concurrent_num; i++) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);	// avoid mysql dead lock
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				consumer_threads.execute(new Runnable() {
					@Override
					public void run() {
						String consumerUuid = UUID.randomUUID().toString();	// consumer uuid
						while (true) {
							int topic_beat = 1;
							Topic topic = (Topic) getDestination();
							IMessageService service = connectionFactory.getMessageService();
							
							// load life topic
							List<TopicMessage> list = service.selectListTopic(topic.getTopicName(), 
									connectionFactory.getTopic_beat() * 3, consumerUuid, connectionFactory.getTopic_pagesize());
							
							if (list != null && list.size() > 0) {
								for (TopicMessage message : list) {
									StatusEnum invokeStatus = null;
									try {
										invokeStatus = getMessageListener().onMessage(message);
									} catch (Exception e) {
										e.printStackTrace();
										invokeStatus = StatusEnum.FAIL;
									}
									logger.info(">>>>>>>>> xxl-mq topic comsumer run, status:{}, message:{}", 
											JacksonUtil.writeValueAsString(invokeStatus), JacksonUtil.writeValueAsString(message));
									if (invokeStatus != null && invokeStatus == StatusEnum.SUCCESS) {
										service.addTopicLog(message.getId(), consumerUuid);
									}
								}
								topic_beat = 1;
							} else {
								topic_beat = connectionFactory.getTopic_beat();
							}
							try {
								TimeUnit.SECONDS.sleep(topic_beat);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				});	
			}
		} else if(destination instanceof Queue) {
			if (!consumer_concurrent_switch) {
				consumer_concurrent_num = 1;	// queue do not use concurrent, limit 1 thread every consumer
			}
			for (int i = 0; i < consumer_concurrent_num; i++) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);	// avoid mysql dead lock
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				consumer_threads.execute(new Runnable() {
					@Override
					public void run() {
						String consumerUuid = UUID.randomUUID().toString();	// consumer uuid
						while (true) {
							int queue_beat = 1;
							Queue queue = (Queue) getDestination();
							IMessageService service = connectionFactory.getMessageService();
							
							List<QueueMessage> list = null;
							
							if(consumer_concurrent_switch){
								// queue data hash id 01 : id % N ... id % N-1 (有瑕疵, 但是简单实用)
								// queue data hash id 02 : virtual node ~ consumer uuid ~ hash(id), 雪崩-虚拟节点 (ConsumerConsistencyHashUtil 设计优雅, 但是不够kiss)
								
								// add queue comsumer
								int ret = service.freshQueueConsumer(queue.getQueueName(), consumerUuid);
								if (ret < 1) {
									service.addQueueConsumer(queue.getQueueName(), consumerUuid);
								}
								
								// get modulus by consumer list
								Map<String, Object> result = service.getQueueConsumerRank(queue.getQueueName(), consumerUuid);
								int rank = 0;
								int count = 0;
								if (result != null) {
									rank = ((Double) result.get("rank")).intValue();
									count = ((Long) result.get("count")).intValue();
								}
								if (count == 0) {
									try {
										TimeUnit.SECONDS.sleep(connectionFactory.getQueue_beat());
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									return;
								}
								
								// concurrent
								list = service.selectListQueueByConsumer(StatusEnum.SUCCESS.getStatus(), connectionFactory.getQueue_pagesize(), rank, count);
							} else {
								// if not concurrent, need contention for lock
								
								// add queue lock
								QueueLock lock = service.getQueueLock(queue.getQueueName());
								if (lock == null) {
									service.insertQueueLock(queue.getQueueName(), consumerUuid);
								}
									
								// compete queue lock , success condition: my lock(in lifetime, keep it)、free lock(over lifetime, beat*3 )
								int ret = service.competeQueueLock(queue.getQueueName(), consumerUuid, connectionFactory.getQueue_beat() * 3);
								
								// compete lock fail
								if (ret < 1) {
									try {
										TimeUnit.SECONDS.sleep(connectionFactory.getQueue_beat());
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									return;
								}
								
								// compete lock success
								logger.debug("queue consumer compete lock success, queueName:{}, consumerUuid:{}", queue.getQueueName(), consumerUuid);
								list = service.selectListQueue(StatusEnum.SUCCESS.getStatus(), connectionFactory.getQueue_pagesize());
							}
							
							// process queue data
							if (list != null && list.size() > 0) {
								for (QueueMessage message : list) {
									int ret = service.descQueueRetryCount(message.getId(), StatusEnum.SUCCESS.getStatus());
									if (ret > 0) {
										StatusEnum invokeStatus = null;
										try {
											invokeStatus = getMessageListener().onMessage(message);
										} catch (Exception e) {
											e.printStackTrace();
											invokeStatus = StatusEnum.FAIL;
										}
										logger.info(">>>>>>>>> xxl-mq queue comsumer running, consumerUuid:{}, status:{}, message:{}", 
												consumerUuid, JacksonUtil.writeValueAsString(invokeStatus), JacksonUtil.writeValueAsString(message));
										if (invokeStatus == null || invokeStatus != StatusEnum.SUCCESS) {
											service.updateQueueStatus(message.getId(), StatusEnum.FAIL.getStatus());
										}
									}
								}
								queue_beat = 1;
							} else {
								queue_beat = connectionFactory.getQueue_beat();
							}
							
							// little sleep
							try {
								TimeUnit.SECONDS.sleep(queue_beat);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				});	
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
	public ExecutorService getConsumer_threads() {
		return consumer_threads;
	}
	public void setConsumer_threads(ExecutorService consumer_threads) {
		this.consumer_threads = consumer_threads;
	}
	public boolean isConsumer_concurrent_switch() {
		return consumer_concurrent_switch;
	}
	public void setConsumer_concurrent_switch(boolean consumer_concurrent_switch) {
		this.consumer_concurrent_switch = consumer_concurrent_switch;
	}
	public int getConsumer_concurrent_num() {
		return consumer_concurrent_num;
	}
	public void setConsumer_concurrent_num(int consumer_concurrent_num) {
		this.consumer_concurrent_num = consumer_concurrent_num;
	}
	
}
