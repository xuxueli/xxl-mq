package com.xxl.mq.factory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.xxl.mq.spring.IMessageService;


public class ConnectionFactory {
	
	@Autowired
	private IMessageService messageService;
	private int topic_beat = 5; 				// topic beat /seconds (beat for topic check; over topic-lifetime(3 * queue) topic will be remove)
	private int topic_pagesize = 100;			// topic load pagesize
	private boolean topic_cleandead = true; 	// topic clean switch, if over topic-lifetime(3 * queue)
	private int queue_beat = 5;					// queue beat /seconds (beat for queue check; over lock-lifetime(3 * queue) lock will be update/clean; over consumer-lifetime(3*queue) consumer will be remove)
	private int queue_pagesize = 100;			// queue load pagesize
	private boolean queue_cleansucess = false;	// queue clean switch, if queue-status is success every (10 * queue)
	
	public IMessageService getMessageService() {
		return messageService;
	}
	public void setMessageService(IMessageService messageService) {
		this.messageService = messageService;
	}
	public int getTopic_beat() {
		return topic_beat;
	}
	public void setTopic_beat(int topic_beat) {
		this.topic_beat = topic_beat;
	}
	public int getTopic_pagesize() {
		return topic_pagesize;
	}
	public void setTopic_pagesize(int topic_pagesize) {
		this.topic_pagesize = topic_pagesize;
	}
	public boolean isTopic_cleandead() {
		return topic_cleandead;
	}
	public void setTopic_cleandead(boolean topic_cleandead) {
		this.topic_cleandead = topic_cleandead;
	}
	public int getQueue_beat() {
		return queue_beat;
	}
	public void setQueue_beat(int queue_beat) {
		this.queue_beat = queue_beat;
	}
	public int getQueue_pagesize() {
		return queue_pagesize;
	}
	public void setQueue_pagesize(int queue_pagesize) {
		this.queue_pagesize = queue_pagesize;
	}
	public boolean isQueue_cleansucess() {
		return queue_cleansucess;
	}
	public void setQueue_cleansucess(boolean queue_cleansucess) {
		this.queue_cleansucess = queue_cleansucess;
	}

	// 监控线程
	private ExecutorService boss_threads;
	public void init(){
		boss_threads = Executors.newCachedThreadPool();
		
		// clean dead topic
		if (topic_cleandead) {
			boss_threads.execute(new Runnable() {
				@Override
				public void run() {
					while (true) {
						messageService.cleanDeadTopic(topic_beat * 3);
						try {
							TimeUnit.SECONDS.sleep(topic_beat * 3);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		// clean dead queue lock
		boss_threads.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					messageService.cleanDeadQueueLock(queue_beat * 3);
					try {
						TimeUnit.SECONDS.sleep(queue_beat * 3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		// queue clean switch, if queue-status is success every (10 * queue)
		if (queue_cleansucess) {
			boss_threads.execute(new Runnable() {
				@Override
				public void run() {
					while (true) {
						messageService.cleanSuccessQueueMessage();
						try {
							TimeUnit.SECONDS.sleep(queue_beat * 3);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		// clean dead queue consumer
		boss_threads.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					messageService.cleanDeadQueueConsumer(queue_beat * 3);
					try {
						TimeUnit.SECONDS.sleep(queue_beat * 3);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
	}
	
}
