package com.xxl.mq.client.rpc.netcom.netty.client;

import com.xxl.mq.client.rpc.registry.ZkServiceDiscovery;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.concurrent.ConcurrentHashMap;

/**
 * connect pool
 * @author xuxueli 2015-11-5 22:05:38
 */
public class NettyClientPool {
	
	private GenericObjectPool<NettyClientPoolProxy> pool;
	public NettyClientPool(String host, int port) {
		pool = new GenericObjectPool<NettyClientPoolProxy>(new NettyClientPoolFactory(host, port));
		pool.setTestOnBorrow(true);
		pool.setMaxTotal(3);
	}
	
	public GenericObjectPool<NettyClientPoolProxy> getPool(){
		return this.pool;
	}

	// serverAddress : [NettyClientPoolProxy01, NettyClientPoolProxy02]
	private static ConcurrentHashMap<String, NettyClientPool> clientPoolMap = new ConcurrentHashMap<String, NettyClientPool>();
	public static GenericObjectPool<NettyClientPoolProxy> getPool(String className)
			throws Exception {

		// valid serverAddress
		String serverAddress = ZkServiceDiscovery.discover(className);

		if (serverAddress == null || serverAddress.trim().length() == 0) {
			throw new IllegalArgumentException(">>>>>>>>>>>> serverAddress is null");
		}

		// get from pool
		NettyClientPool clientPool = clientPoolMap.get(serverAddress);
		if (clientPool != null) {
			return clientPool.getPool();
		}

		// init pool
		String[] array = serverAddress.split(":");
		String host = array[0];
		int port = Integer.parseInt(array[1]);

		clientPool = new NettyClientPool(host, port);
		clientPoolMap.put(serverAddress, clientPool);
		return clientPool.getPool();
	}
	
}
