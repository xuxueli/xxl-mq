package com.xxl.mq.client.rpc.netcom;

import com.xxl.mq.client.rpc.netcom.common.codec.RpcRequest;
import com.xxl.mq.client.rpc.netcom.common.codec.RpcResponse;
import com.xxl.mq.client.rpc.netcom.netty.server.NettyServer;
import com.xxl.mq.client.rpc.registry.ZkServiceRegistry;
import com.xxl.mq.client.service.BrokerService;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * netcom init
 * @author xuxueli 2015-10-31 22:54:27
 *
 * <bean class="com.xxl.rpc.netcom.NetComFactory" />
 */
public class NetComServerFactory implements InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(NetComServerFactory.class);

	// ---------------------- server config ----------------------
	private static int port = 6080;
	private BrokerService brokerService;
	public void setPort(int port) {
		this.port = port;
	}
	public void setBrokerService(BrokerService brokerService) {
		this.brokerService = brokerService;
	}

	// ---------------------- server init ----------------------
	@Override
	public void afterPropertiesSet() throws Exception {
		serviceMap.put(BrokerService.class.getName(), brokerService);

		// init rpc provider
		new NettyServer().start(6080);

	}

	private static Executor executor = Executors.newCachedThreadPool();
	static {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (true) {
					// registry
					try {
						ZkServiceRegistry.registerServices(port, serviceMap.keySet());
						TimeUnit.SECONDS.sleep(10);
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
	private static Map<String, Object> serviceMap = new HashMap<String, Object>();
	public static RpcResponse invokeService(RpcRequest request, Object serviceBean) {
		if (serviceBean==null) {
			serviceBean = serviceMap.get(request.getClassName());
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
