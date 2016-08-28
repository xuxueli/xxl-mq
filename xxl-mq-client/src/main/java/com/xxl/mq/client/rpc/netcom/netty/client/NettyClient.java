package com.xxl.mq.client.rpc.netcom.netty.client;

import com.xxl.mq.client.rpc.netcom.common.codec.RpcCallbackFuture;
import com.xxl.mq.client.rpc.netcom.common.codec.RpcRequest;
import com.xxl.mq.client.rpc.netcom.common.codec.RpcResponse;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty client
 * @author xuxueli 2015-11-24 22:25:15
 */
public class NettyClient {
	private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

	private long timeoutMillis;
	public NettyClient(long timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
	}

	public RpcResponse send(RpcRequest request) throws Exception {
		// client pool	[tips03 : may save 35ms/100invoke if move it to constructor, but it is necessary. cause by ConcurrentHashMap.get]
        GenericObjectPool<NettyClientPoolProxy> clientPool = NettyClientPool.getPool(request.getClassName());
        
        // client proxt
        NettyClientPoolProxy clientPoolProxy = null;
		try {
			// future init	[tips04 : may save 20ms/100invoke if remove and wait for channel instead, but it is necessary. cause by ConcurrentHashMap.get]
			RpcCallbackFuture future = new RpcCallbackFuture(request);
			RpcCallbackFuture.futurePool.put(request.getRequestId(), future);
			
			// rpc invoke
			clientPoolProxy = clientPool.borrowObject();
			clientPoolProxy.send(request);
			
			// future get
			return future.get(timeoutMillis);
		} catch (Exception e) {
			logger.error("", e);
			throw e;
		} finally{
			RpcCallbackFuture.futurePool.remove(request.getRequestId());
			clientPool.returnObject(clientPoolProxy);
		}
		
	}

}
