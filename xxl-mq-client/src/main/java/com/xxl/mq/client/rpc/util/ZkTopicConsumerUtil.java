package com.xxl.mq.client.rpc.util;

import com.xxl.mq.client.XxlMqConsumer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zookeeper service registry
 * @author xuxueli 2015-10-29 14:43:46
 */
public class ZkTopicConsumerUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZkTopicConsumerUtil.class);

	// ------------------------------ zookeeper client ------------------------------
	private static ZooKeeper zooKeeper;
	private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);
	private static ZooKeeper getInstance(){
		if (zooKeeper==null) {
			try {
				if (INSTANCE_INIT_LOCK.tryLock(5, TimeUnit.SECONDS)) {

					// init zookeeper
					/*final CountDownLatch countDownLatch = new CountDownLatch(1);
					countDownLatch.countDown();
					countDownLatch.await();*/
					zooKeeper = new ZooKeeper(Environment.ZK_ADDRESS, 10000, new Watcher() {
						@Override
						public void process(WatchedEvent event) {

							// session expire, close old and create new
							if (event.getState() == Event.KeeperState.Expired) {
								try {
									zooKeeper.close();
								} catch (InterruptedException e) {
									logger.error("", e);
								}
								zooKeeper = null;
							}

							// refresh service address
							if (event.getType() == Event.EventType.NodeDataChanged){
								String path = event.getPath();
								if (path!=null && path.startsWith(Environment.ZK_CONSUMER_PATH)) {
									// add one-time watch
									try {
										zooKeeper.exists(path, true);
									} catch (Exception e) {
										logger.error("", e);
									}
									// broadcase message
									String name = path.substring(Environment.ZK_CONSUMER_PATH.length()+1, path.length());
									String data = null;
									try {
										byte[] resultData = zooKeeper.getData(path, true, null);
										if (resultData != null) {
											data = new String(resultData);
										}
									} catch (Exception e) {
										logger.error("", e);
									}
									XxlMqConsumer.pushTopicMessage(name, data);
								}
							}

						}
					});

					// init base path
					Stat baseStat = zooKeeper.exists(Environment.ZK_BASE_PATH, false);
					if (baseStat == null) {
						zooKeeper.create(Environment.ZK_BASE_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}

					// init consumer path
					Stat stat =zooKeeper.exists(Environment.ZK_CONSUMER_PATH, false);
					if (stat == null) {
						zooKeeper.create(Environment.ZK_CONSUMER_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}

					logger.info(">>>>>>>>> xxl-rpc zookeeper connnect success.");
				}
			} catch (InterruptedException e) {
				logger.error("", e);
			} catch (IOException e) {
				logger.error("", e);
			} catch (KeeperException e) {
				logger.error("", e);
			}
		}
		if (zooKeeper == null) {
			throw new NullPointerException(">>>>>>>>>>> xxl-rpc, zookeeper connect fail.");
		}
		return zooKeeper;
	}

	// ------------------------------ topic watch ------------------------------
	/**
	 * register service
	 */
	public static void watchTopic(Set<String> topicKeyList) throws KeeperException, InterruptedException {

		// valid
		if (topicKeyList==null || topicKeyList.size()==0) {
			return;
		}

		// muit watch topic key
		for (String topicKey : topicKeyList) {

			// "topic key" path : /xxl-rpc/topic-key
			String topicKeyPath = Environment.ZK_CONSUMER_PATH.concat("/").concat(topicKey);

			// watch
			Stat topicKeyPathStat = getInstance().exists(topicKeyPath, true);
			if (topicKeyPathStat == null) {
				getInstance().create(topicKeyPath, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				getInstance().exists(topicKeyPath, true);
			}

			logger.info(">>>>>>>>>>> xxl-rpc topic consumer watch topic item, topicKey:{}, topicKeyPath:{}", topicKey, topicKeyPath);
		}

	}

	// ------------------------------ topic broadcast ------------------------------
	public static Stat broadcast(String name, String data) {
		try {

			// "base" path : /xxl-rpc
			Stat stat = getInstance().exists(Environment.ZK_CONSUMER_PATH, false);
			if (stat == null) {
				getInstance().create(Environment.ZK_CONSUMER_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}

			// "topic key" path : /xxl-rpc/topic-key
			String topicKeyPath = Environment.ZK_CONSUMER_PATH.concat("/").concat(name);

			// watch
			Stat topicKeyPathStat = getInstance().exists(topicKeyPath, true);
			if (topicKeyPathStat == null) {
				getInstance().create(topicKeyPath, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				topicKeyPathStat = getInstance().exists(topicKeyPath, true);
			}

			Stat ret = zooKeeper.setData(topicKeyPath, data.getBytes(), topicKeyPathStat.getVersion());
			return ret;
		} catch (Exception e) {
			logger.error("", e);
		}
		return null;
	}

}