package com.xxl.mq.client.rpc.netcom;

import com.xxl.mq.client.rpc.netcom.common.codec.RpcRequest;
import com.xxl.mq.client.rpc.netcom.common.codec.RpcResponse;
import com.xxl.mq.client.rpc.netcom.netty.client.NettyClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * rpc proxy
 * @author xuxueli 2015-10-29 20:18:32
 */
public class NetComClientProxy implements FactoryBean<Object>, InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(NetComClientProxy.class);	 
	// [tips01: save 30ms/100invoke. why why why??? with this logger, it can save lots of time.]

	// ---------------------- config ----------------------
	private Class<?> iface;
	private long timeoutMillis = 5000;
	private String registryKey;

	public NetComClientProxy(Class<?> iface, long timeoutMillis, String registryKey) {

		this.iface = iface;
		this.timeoutMillis = timeoutMillis;
		this.registryKey = registryKey;
		try {
			this.afterPropertiesSet();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	// ---------------------- init client, operate ----------------------
	NettyClient client = null;
	@Override
	public void afterPropertiesSet() throws Exception {
		client = new NettyClient(5000);
	}

	@Override
	public Object getObject() throws Exception {
		return Proxy.newProxyInstance(Thread.currentThread()
				.getContextClassLoader(), new Class[] { iface },
				new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						
						// request
						RpcRequest request = new RpcRequest();
						request.setRegistryKey(registryKey);
	                    request.setRequestId(UUID.randomUUID().toString());
	                    request.setCreateMillisTime(System.currentTimeMillis());
	                    request.setClassName(method.getDeclaringClass().getName());
	                    request.setMethodName(method.getName());
	                    request.setParameterTypes(method.getParameterTypes());
	                    request.setParameters(args);

						if (request.getRegistryKey()==null) {
							request.setRegistryKey(request.getClassName());
						}
	                    
	                    // send
	                    RpcResponse response = client.send(request);
	                    
	                    // valid response
						if (response == null) {
							logger.error(">>>>>>>>>>> xxl-rpc netty response not found.");
							throw new Exception(">>>>>>>>>>> xxl-rpc netty response not found.");
						}
	                    if (response.isError()) {
	                        throw response.getError();
	                    } else {
	                        return response.getResult();
	                    }
	                   
					}
				});
	}
	@Override
	public Class<?> getObjectType() {
		return iface;
	}
	@Override
	public boolean isSingleton() {
		return false;
	}

}
