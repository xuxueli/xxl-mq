package com.xxl.mq.client.rpc.netcom;

import com.xxl.mq.client.rpc.netcom.codec.model.RpcRequest;
import com.xxl.mq.client.rpc.netcom.codec.model.RpcResponse;
import com.xxl.mq.client.rpc.netcom.server.NettyServer;
import com.xxl.mq.client.rpc.util.ZkServiceUtil;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * netcom init
 * @author xuxueli 2015-10-31 22:54:27
 */
public class NetComServerFactory {
	private static final Logger logger = LoggerFactory.getLogger(NetComServerFactory.class);

	// ---------------------- server start ----------------------
	private static int port;
	private static Map<String, Object> regitsryMap;
	public NetComServerFactory(int port, Map<String, Object> serviceMap) throws Exception {
		this.port = port;
		this.regitsryMap = serviceMap;

		// setver start
		new NettyServer().start(port);
	}

	// refresh registry address
	private static Executor executor = Executors.newCachedThreadPool();
	static {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					// registry
					try {
						ZkServiceUtil.registry(port, regitsryMap.keySet());
						TimeUnit.SECONDS.sleep(60);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (KeeperException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	// ---------------------- server invoke ----------------------
	/**
	 * init local rpc service map
	 */

	public static RpcResponse invokeService(RpcRequest request, Object serviceBean) {
		if (serviceBean==null) {
			serviceBean = regitsryMap.get(request.getRegistryKey());
		}
		if (serviceBean == null) {
			// TODO
		}

		RpcResponse response = new RpcResponse();
		response.setRequestId(request.getRequestId());

		try {
			Class<?> serviceClass = serviceBean.getClass();
			String methodName = request.getMethodName();
			Class<?>[] parameterTypes = request.getParameterTypes();
			Object[] parameters = request.getParameters();

            /*Method method = serviceClass.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(serviceBean, parameters);*/

			FastClass serviceFastClass = FastClass.create(serviceClass);
			FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);

			Object result = serviceFastMethod.invoke(serviceBean, parameters);

			response.setResult(result);
		} catch (Throwable t) {
			t.printStackTrace();
			response.setError(t);
		}

		return response;
	}
	
}
