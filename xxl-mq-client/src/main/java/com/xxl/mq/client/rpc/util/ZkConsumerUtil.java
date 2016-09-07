package com.xxl.mq.client.rpc.util;

import com.xxl.mq.client.service.annotation.MqConsumer;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zookeeper service registry
 * @author xuxueli 2015-10-29 14:43:46
 */
public class ZkConsumerUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZkConsumerUtil.class);

	// ------------------------------ zookeeper client ------------------------------
	private static ZooKeeper zooKeeper;
	private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);
	private static ZooKeeper getInstance(){
		if (zooKeeper==null) {
			try {
				if (INSTANCE_INIT_LOCK.tryLock(5, TimeUnit.SECONDS)) {
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
							logger.info("" + event);
							if (event.getType() == Event.EventType.NodeChildrenChanged || event.getState() == Event.KeeperState.SyncConnected) {
								if (event.getPath()!=null && event.getPath().startsWith(Environment.ZK_CONSUMER_PATH)) {
									try {
										discoverConsumers();
									} catch (Exception e) {
										logger.error("", e);
									}
								}
							}

						}
					});

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

	// ------------------------------ register service ------------------------------
	private static final String localAddress = IpUtil.getAddress(6080);
	/**
	 * register service
	 */
	public static void registerConsumers(Set<String> registryKeyList) throws KeeperException, InterruptedException {

		// valid
		if (registryKeyList==null || registryKeyList.size()==0) {
			return;
		}

		// address
		String address = localAddress;

		// "base" path : /xxl-rpc
		Stat stat = getInstance().exists(Environment.ZK_CONSUMER_PATH, false);
		if (stat == null) {
			getInstance().create(Environment.ZK_CONSUMER_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}

		// muit registry
		for (String registryKey : registryKeyList) {

			// "resigtry key" path : /xxl-rpc/registry-key
			String registryKeyPath = Environment.ZK_CONSUMER_PATH.concat("/").concat(registryKey);
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
			logger.info(">>>>>>>>>>> xxl-rpc registe consumer item, registryKey:{}, address:{}, registryKeyAddressPath:{}", registryKey, address, registryKeyAddressPath);
		}

	}

	// ------------------------------ private discover service ------------------------------
	private static Executor executor = Executors.newCachedThreadPool();
	static {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						TimeUnit.SECONDS.sleep(60L);
						//discoverConsumers();
					} catch (Exception e) {
						logger.error("", e);
					}

				}
			}
		});
	}

	// "resigtry key's address" path : /xxl-rpc/registry-key/address
	private static volatile ConcurrentMap<String, Set<String>> consumerAddress = new ConcurrentHashMap<String, Set<String>>();

	// only fresh node that used
	private static void discoverConsumers() {
		if (consumerAddress ==null || consumerAddress.size()==0) {
			return;
		}

		try {
			for (String registryKey: consumerAddress.keySet()) {
				Set<String> addressSet = new HashSet<String>();

				// "resigtry key" path : /xxl-rpc/registry-key
				String registryKeyPath = Environment.ZK_CONSUMER_PATH.concat("/").concat(registryKey);
				Stat registryKeyPathStat = getInstance().exists(registryKeyPath, true);		// watch "create/delete/setData" of path

				if (registryKeyPathStat != null) {

					// "resigtry key's address" path : /xxl-rpc/registry-key/address
					List<String> addressList = getInstance().getChildren(registryKeyPath, true);	// watch "delete" of path and "create/delete" of child path
					if (addressList!=null && addressList.size()>0) {
						addressSet.addAll(addressList);
					}

				}

				consumerAddress.put(registryKey, addressSet);
				logger.info(">>>>>>>>>>> xxl-rpc, discover consumer item, registryKey:{}, addressSet:{}", registryKey, addressSet);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	// ------------------------------ public util ------------------------------
	public static ActiveInfo isActice(MqConsumer annotation) {
		// info
		String name = annotation.value();
		MqConsumer.MqType type = annotation.type();

		// load address set
		Set<String> addressSet = consumerAddress.get(name);
		if (addressSet == null) {
			consumerAddress.put(name, new HashSet<String>());
			discoverConsumers();
			addressSet = consumerAddress.get(name);
		}
		if (addressSet.size()==0) {
			return null;
		}

		// parse rank
		TreeSet<String> sortSet = new TreeSet<String>(addressSet);
		int index = 0;
		for (String item: sortSet) {
			if (item.equals(localAddress)) {
				break;
			}
			index++;
		}

		// for biz
		switch (type) {
			case TOPIC: {
				// TODO
			}
			case SERIAL_QUEUE: {
				if (index == 0) {
					return new ActiveInfo(index, sortSet.size());
				}
			}
			case QUEUE: {
				return new ActiveInfo(index, sortSet.size());
			}
		}
		return null;
	}

	public static class ActiveInfo{
		public int rank;
		public int total;
		public ActiveInfo(int rank, int total) {
			this.rank = rank;
			this.total = total;
		}
	}

}