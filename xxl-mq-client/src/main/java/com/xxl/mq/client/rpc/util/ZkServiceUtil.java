package com.xxl.mq.client.rpc.util;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zookeeper service registry
 * @author xuxueli 2015-10-29 14:43:46
 */
public class ZkServiceUtil {
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceUtil.class);

	// ------------------------------ zookeeper client ------------------------------
	private static ZooKeeper zooKeeper;
	private static ReentrantLock INSTANCE_INIT_LOCK = new ReentrantLock(true);

	private static ZooKeeper getInstance(){
		if (zooKeeper==null) {
			try {
				if (INSTANCE_INIT_LOCK.tryLock(2, TimeUnit.SECONDS)) {
					/*final CountDownLatch countDownLatch = new CountDownLatch(1);
					countDownLatch.countDown();
					countDownLatch.await();*/
					zooKeeper = new ZooKeeper(Environment.ZK_ADDRESS, 30000, new Watcher() {
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
							// add One-time trigger, ZooKeeper的Watcher是一次性的，用过了需要再注册
							try {
								String znodePath = event.getPath();
								if (znodePath != null) {
									zooKeeper.exists(znodePath, true);
								}
							} catch (KeeperException e) {
								logger.error("", e);
							} catch (InterruptedException e) {
								logger.error("", e);
							}

							// refresh service address
							if (event.getType() == Event.EventType.NodeChildrenChanged || event.getState() == Event.KeeperState.SyncConnected) {
								freshRegistryAddresss();
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
    /**
     * register service
	 * {
	 *     registry-key1:[address1, address2, address3]
	 *     registry-key2:[address1, address2, address3]
	 * }
     */
    public static void registry(int port, Set<String> serviceList) throws KeeperException, InterruptedException {
    	// valid
    	if (port < 1 || (serviceList==null || serviceList.size()==0)) {
    		return;
    	}

    	// init address: ip : port
    	String ip = null;
		try {
			ip = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		if (ip == null) {
			return;
		}
		String serverAddress = ip + ":" + port;

		// base path
		Stat stat = getInstance().exists(Environment.ZK_SERVICES_PATH, true);
		if (stat == null) {
			getInstance().create(Environment.ZK_SERVICES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		}

		// register
		for (String interfaceName : serviceList) {

			// init servicePath prefix : servicePath : xxl-rpc/interfaceName/serverAddress(ip01:port9999)
			String ifacePath = Environment.ZK_SERVICES_PATH.concat("/").concat(interfaceName);
			String addressPath = Environment.ZK_SERVICES_PATH.concat("/").concat(interfaceName).concat("/").concat(serverAddress);

			// ifacePath(parent) path must be PERSISTENT
			Stat ifacePathStat = getInstance().exists(ifacePath, true);
			if (ifacePathStat == null) {
				getInstance().create(ifacePath, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}

			// register service path must be EPHEMERAL
			Stat addreddStat = getInstance().exists(addressPath, true);
			if (addreddStat == null) {
				String path = getInstance().create(addressPath, serverAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
			}
			logger.info(">>>>>>>>>>> xxl-mq register success, interfaceName:{}, serverAddress:{}, addressPath:{}", interfaceName, serverAddress, addressPath);
		}

    }

	// ------------------------------ discover service ------------------------------
	private static Executor executor = Executors.newCachedThreadPool();
	static {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					freshRegistryAddresss();
					try {
						TimeUnit.SECONDS.sleep(30L);
					} catch (InterruptedException e) {
						logger.error("", e);
					}
				}
			}
		});
	}

	/**
	 * 	/xxl-rpc/iface1/address1
	 * 	/xxl-rpc/iface1/address2
	 * 	/xxl-rpc/iface1/address3
	 * 	/xxl-rpc/iface2/address1
	 */
	private static volatile ConcurrentMap<String, Set<String>> registryKeyToAddresss = new ConcurrentHashMap<String, Set<String>>();

	public static void freshRegistryAddresss(){
		ConcurrentMap<String, Set<String>> tempMap = new ConcurrentHashMap<String, Set<String>>();
		try {
			// iface list
			List<String> registryKeyList = getInstance().getChildren(Environment.ZK_SERVICES_PATH, true);

			if (registryKeyList!=null && registryKeyList.size()>0) {
				for (String registryKey : registryKeyList) {

					// address list
					String ifacePath = Environment.ZK_SERVICES_PATH.concat("/").concat(registryKey);
					List<String> addressList = getInstance().getChildren(ifacePath, true);

					if (addressList!=null && addressList.size() > 0) {
						Set<String> addressSet = new HashSet<String>();
						for (String address : addressList) {

							// data from address
							String addressPath = ifacePath.concat("/").concat(address);
							byte[] bytes = getInstance().getData(addressPath, false, null);
							addressSet.add(new String(bytes));
						}
						tempMap.put(registryKey, addressSet);
					}
				}
				registryKeyToAddresss = tempMap;
				logger.info(">>>>>>>>>>> xxl-rpc fresh registryKeyToAddresss success: {}", registryKeyToAddresss);
			}

		} catch (KeeperException e) {
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

	public static String discover(String registryKey) {
		Set<String> addressSet = registryKeyToAddresss.get(registryKey);
		if (addressSet==null || addressSet.size()==0) {
			freshRegistryAddresss();
			addressSet = registryKeyToAddresss.get(registryKey);
			if (addressSet==null || addressSet.size()==0) {
				return null;
			}
		}

		String address;
		List<String> addressArr = new ArrayList<String>(addressSet);
		int size = addressSet.toArray().length;
		if (size == 1) {
			address = addressArr.get(0);
		} else {
			address = addressArr.get(new Random().nextInt(size));
		}
		return address;
	}


}