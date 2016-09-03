package com.xxl.mq.client.rpc.util;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zookeeper service registry
 * @author xuxueli 2015-10-29 14:43:46
 */
public class ZkServiceRegistryUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistryUtil.class);

	// ------------------------------ zookeeper client ------------------------------
	private static ZooKeeper zooKeeper;
	private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);

	private static ZooKeeper getInstance(){
		if (zooKeeper==null) {
			try {
				if (INSTANCE_INIT_LOCK.tryLock(2, TimeUnit.SECONDS)) {
					zooKeeper = ZookeeperUtil.getNewInstance(Environment.ZK_ADDRESS, new Watcher() {
						@Override
						public void process(WatchedEvent watchedEvent) {
							// session expire, close old and create new
							if (watchedEvent.getState() == Event.KeeperState.Expired) {
								try {
									zooKeeper.close();
								} catch (InterruptedException e) {
									logger.error("", e);
								}
								zooKeeper = null;
							}

							// valid rpc path
							String path = watchedEvent.getPath();
							if (path!=null && path.startsWith(Environment.ZK_SERVICES_PATH)) {

								// add One-time trigger, ZooKeeper的Watcher是一次性的，用过了需要再注册
								try {
									String znodePath = watchedEvent.getPath();
									if (znodePath != null) {
										zooKeeper.exists(znodePath, true);
									}
								} catch (KeeperException e) {
									logger.error("", e);
								} catch (InterruptedException e) {
									logger.error("", e);
								}

								// rpc biz
								Event.EventType eventType = watchedEvent.getType();
								if (eventType == Event.EventType.NodeCreated) {
									System.out.println("NodeCreated:" + path);
								} else if (eventType == Event.EventType.NodeDeleted) {
									System.out.println("NodeDeleted:" + path);
								} else if (eventType == Event.EventType.NodeDataChanged) {
									System.out.println("NodeDataChanged:" + path);
								} else if (eventType == Event.EventType.NodeChildrenChanged) {
									System.out.println("NodeChildrenChanged:" + path);
								}

							}
						}
					});

					/**
					 * RPC目录结构: 根目录/ registryKey / address
					 *
					 * 为了实现监听RPC服务节点变动的目的,有以下两种实现方式:
					 * 	1、exits("/根目录 / registryKey") + NodeChildrenChanged 方式: 遍历所有服务注册节点, 全部watch(可优化,只watch项目需要的), 当子节点变动(即服务注册、摘除)时,会触发事件;
					 * 	2、exits("/根目录") + NodeCreated/NodeDeleted 方式: 只需要watch跟地址, 当节点变动时, 根据路径判断对应的服务, 简洁;
                     */
					// watch rpc root path
					//ZookeeperUtil.createWithParent(zooKeeper, Environment.ZK_SERVICES_PATH);
					// watch rpc registry key path
					//ZookeeperUtil.getChildListData(zooKeeper, Environment.ZK_SERVICES_PATH);
					try {
						zooKeeper.getChildren(Environment.ZK_SERVICES_PATH, true);
					} catch (KeeperException e) {
						e.printStackTrace();
					}

					logger.info(">>>>>>>>> xxl-rpc zookeeper connnect success.");
				}
			} catch (InterruptedException e) {
				logger.error("", e);
			} catch (IOException e) {
				logger.error("", e);
			}
		}
		if (zooKeeper == null) {
			throw new NullPointerException(">>>>>>>>>>> xxl-rpc, zookeeper connect fail.");
		}
		return zooKeeper;
	}

	public static void main(String[] args) {
		try {

			if (false) {
				getInstance().exists(Environment.ZK_SERVICES_PATH, true);
				getInstance().exists(Environment.ZK_SERVICES_PATH, true);
				getInstance().exists(Environment.ZK_SERVICES_PATH, true);
			} else {
				String path = Environment.ZK_SERVICES_PATH + "/key";
				ZookeeperUtil.createWithParent(getInstance(), path);
				ZookeeperUtil.deletePath(getInstance(), path);
			}
			TimeUnit.SECONDS.sleep(99999);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ------------------------------ register service ------------------------------


}