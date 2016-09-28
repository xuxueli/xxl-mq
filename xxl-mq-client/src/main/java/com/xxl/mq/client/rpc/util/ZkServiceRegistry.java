package com.xxl.mq.client.rpc.util;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zookeeper service registry
 * @author xuxueli 2015-10-29 14:43:46
 */
public class ZkServiceRegistry {
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceRegistry.class);

	// ------------------------------ zookeeper client ------------------------------
	private static ZooKeeper zooKeeper;
	private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);
	private static ZooKeeper getInstance(){
		if (zooKeeper==null) {
			try {
				if (INSTANCE_INIT_LOCK.tryLock(5, TimeUnit.SECONDS)) {

					try {
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

							}
						});

						// init base path
						Stat baseStat = zooKeeper.exists(Environment.ZK_BASE_PATH, false);
						if (baseStat == null) {
							zooKeeper.create(Environment.ZK_BASE_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						}

						// init service path
						Stat serviceStat = zooKeeper.exists(Environment.ZK_SERVICES_PATH, false);
						if (serviceStat == null) {
							zooKeeper.create(Environment.ZK_SERVICES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						}

						logger.info(">>>>>>>>> xxl-rpc zookeeper connnect success.");
					} finally {
						INSTANCE_INIT_LOCK.unlock();
					}
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

	// ------------------------------ register service ------------------------------
    /**
     * register service
     */
    public static void registerServices(int port, Set<String> registryKeyList) throws KeeperException, InterruptedException {

    	// valid
    	if (port < 1 || (registryKeyList==null || registryKeyList.size()==0)) {
    		return;
    	}

    	// address
		String address = IpUtil.getAddress(port);

		// muit registry
		for (String registryKey : registryKeyList) {

			// "resigtry key" path : /xxl-rpc/registry-key
			String registryKeyPath = Environment.ZK_SERVICES_PATH.concat("/").concat(registryKey);
			Stat registryKeyPathStat = getInstance().exists(registryKeyPath, false);
			if (registryKeyPathStat == null) {
				getInstance().create(registryKeyPath, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}

			// "resigtry key's address" path : /xxl-rpc/registry-key/address
			String registryKeyAddressPath = registryKeyPath.concat("/").concat(address);
			Stat addreddStat = getInstance().exists(registryKeyAddressPath, false);
			if (addreddStat == null) {
				String path = getInstance().create(registryKeyAddressPath, address.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);	// must be EPHEMERAL
			}
			logger.info(">>>>>>>>>>> xxl-rpc registe service item, registryKey:{}, address:{}, registryKeyAddressPath:{}", registryKey, address, registryKeyAddressPath);
		}

    }

	public static void main(String[] args) throws KeeperException, InterruptedException {
		registerServices(3333, new HashSet<String>(Arrays.asList("path2")));
		TimeUnit.SECONDS.sleep(9999);
	}

}