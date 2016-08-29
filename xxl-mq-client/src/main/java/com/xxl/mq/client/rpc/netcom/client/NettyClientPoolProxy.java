package com.xxl.mq.client.rpc.netcom.client;

import com.xxl.mq.client.rpc.netcom.model.RpcRequest;
import com.xxl.mq.client.rpc.netcom.model.RpcResponse;
import com.xxl.mq.client.rpc.netcom.codec.NettyDecoder;
import com.xxl.mq.client.rpc.netcom.codec.NettyEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * connetion proxy
 * @author xuxueli
 */
public class NettyClientPoolProxy {
	private static transient Logger logger = LoggerFactory.getLogger(NettyClientPoolProxy.class);
	

}
