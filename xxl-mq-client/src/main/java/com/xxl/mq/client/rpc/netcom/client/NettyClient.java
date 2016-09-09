package com.xxl.mq.client.rpc.netcom.client;

import com.xxl.mq.client.rpc.netcom.codec.NettyDecoder;
import com.xxl.mq.client.rpc.netcom.codec.NettyEncoder;
import com.xxl.mq.client.rpc.netcom.codec.model.RpcCallbackFuture;
import com.xxl.mq.client.rpc.netcom.codec.model.RpcRequest;
import com.xxl.mq.client.rpc.netcom.codec.model.RpcResponse;
import com.xxl.mq.client.rpc.util.ZkServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty client
 * @author xuxueli 2015-11-24 22:25:15
 */
public class NettyClient {
	private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

	private static ConcurrentHashMap<String, Channel> addressToChannel = new ConcurrentHashMap<String, Channel>();
	private static Channel getChannel(String address) throws InterruptedException {
		if (address==null || address.trim().length()==0) {
			return null;
		}

		// load channel
		Channel channel = addressToChannel.get(address);
		if (channel != null && channel.isActive()) {
			return channel;
		}

		String[] array = address.split(":");
		String host = array[0];
		int port = Integer.parseInt(array[1]);

		// init channel
		EventLoopGroup group = new NioEventLoopGroup();
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(group).channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel channel) throws Exception {
						channel.pipeline()
								.addLast(new NettyEncoder(RpcRequest.class))
								.addLast(new NettyDecoder(RpcResponse.class))
								.addLast(new NettyClientHandler());
					}
				})
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_REUSEADDR, true)
				.option(ChannelOption.SO_KEEPALIVE, true);
		channel = bootstrap.connect(host, port).sync().channel();
		if (channel != null && channel.isActive()) {
			addressToChannel.put(address, channel);
		}
		return channel;
	}

	public static void closeAllChannel() {
		for (Channel channel: addressToChannel.values()) {
			if (channel != null) {
				if (channel.isOpen()) {
					channel.close();
				}
			}
		}
	}

	private static void writeAndFlush(Channel channel, RpcRequest request) throws Exception {
		channel.writeAndFlush(request).sync();
	}

	public static RpcResponse send(RpcRequest request) throws Exception {

		try {
			String address = ZkServiceDiscovery.discover(request.getRegistryKey());
			if (address == null) {
				throw new RuntimeException(">>>>>>>>>>> xxl-rpc, no address from service:" + request.getClassName());
			}
			Channel channel = getChannel(address);
			if (channel == null) {
				throw new RuntimeException(">>>>>>>>>>> xxl-rpc, no channel from service:" + request.getClassName());
			}

			// future init	[tips04 : may save 20ms/100invoke if remove and wait for channel instead, but it is necessary. cause by ConcurrentHashMap.get]
			RpcCallbackFuture future = new RpcCallbackFuture(request);
			RpcCallbackFuture.futurePool.put(request.getRequestId(), future);
			
			// rpc invoke
			writeAndFlush(channel, request);
			
			// future get
			return future.get(5000);
		} catch (Exception e) {
			logger.error("", e);
			throw e;
		} finally{
			RpcCallbackFuture.futurePool.remove(request.getRequestId());
		}
		
	}

}
