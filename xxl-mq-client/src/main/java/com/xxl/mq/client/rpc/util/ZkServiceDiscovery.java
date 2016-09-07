package com.xxl.mq.client.rpc.util;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * zookeeper service discovery
 * @author xuxueli 2015-10-29 17:29:32
 */
public class ZkServiceDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ZkServiceDiscovery.class);

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
								if (event.getPath()!=null && event.getPath().startsWith(Environment.ZK_SERVICES_PATH)) {
									try {
										discoverServices();
									} catch (Exception e) {
										logger.error("", e);
									}
								}
							}

						}
					});
					logger.info(">>>>>>>>> xxl-rpc zookeeper connnect success.");
				}
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		if (zooKeeper == null) {
			throw new NullPointerException(">>>>>>>>>>> xxl-rpc, zookeeper connect fail.");
		}
		return zooKeeper;
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
						//discoverServices();
					} catch (Exception e) {
						logger.error("", e);
					}

				}
			}
		});
	}

	// "resigtry key's address" path : /xxl-rpc/registry-key/address
    private static volatile ConcurrentMap<String, Set<String>> serviceAddress = new ConcurrentHashMap<String, Set<String>>();

	// only fresh node that used
    private static void discoverServices() {
		if (serviceAddress==null || serviceAddress.size()==0) {
			return;
		}

		try {
			for (String registryKey: serviceAddress.keySet()) {
				Set<String> addressSet = new HashSet<String>();

				// "resigtry key" path : /xxl-rpc/registry-key
				String registryKeyPath = Environment.ZK_SERVICES_PATH.concat("/").concat(registryKey);
				Stat registryKeyPathStat = getInstance().exists(registryKeyPath, true);		// watch "create/delete/setData" of path

				if (registryKeyPathStat != null) {

					// "resigtry key's address" path : /xxl-rpc/registry-key/address
					List<String> addressList = getInstance().getChildren(registryKeyPath, true);	// watch "delete" of path and "create/delete" of child path
					if (addressList!=null && addressList.size()>0) {
						addressSet.addAll(addressList);
					}

				}

				serviceAddress.put(registryKey, addressSet);
				logger.info(">>>>>>>>>>> xxl-rpc, discover service item, registryKey:{}, addressSet:{}", registryKey, addressSet);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
    }

    // ------------------------------ public util ------------------------------
    public static String discover(String registryKey) {
    	Set<String> addressSet = serviceAddress.get(registryKey);
    	if (addressSet == null) {
			serviceAddress.put(registryKey, new HashSet<String>());
			discoverServices();
			addressSet = serviceAddress.get(registryKey);
		}

		if (addressSet.size()==0) {
			return null;
		}

    	String address;
    	List<String> addressArr = new ArrayList<String>(addressSet);
        if (addressArr.size() == 1) {
            address = addressArr.get(0);
        } else {
        	address = addressArr.get(new Random().nextInt(addressArr.size()));
        }
        return address;
    }

	public static void main(String[] args) throws KeeperException, InterruptedException {
		serviceAddress.put("path2", new HashSet<String>());
		System.out.println(discover("path1"));

	}
}