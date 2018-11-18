package com.xxl.mq.broker.test.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 16/8/29.
 */
public class NBroker {
    private static Logger logger = LoggerFactory.getLogger(NBroker.class);

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup();
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {

                                @Override
                                public void initChannel(SocketChannel channel) throws Exception {
                                    channel.pipeline()
                                            .addLast(new StringDecoder())
                                            .addLast(new StringEncoder())
                                            .addLast(new SimpleChannelInboundHandler<String>() {


                                                ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

                                                @Override
                                                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
                                                    logger.info("handlerAdded");
                                                    Channel incoming = ctx.channel();
                                                    for (Channel channel : channels) {
                                                        channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 加入\n");
                                                    }
                                                    channels.add(ctx.channel());
                                                }

                                                @Override
                                                public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
                                                    logger.info("handlerRemoved");
                                                    Channel incoming = ctx.channel();
                                                    for (Channel channel : channels) {
                                                        channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 离开\n");
                                                    }
                                                    channels.remove(ctx.channel());
                                                }

                                                @Override
                                                protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception { // (4)
                                                    Channel incoming = ctx.channel();
                                                    System.out.println("Broker 消息:"+s);

                                                    for (Channel channel : channels) {
                                                        if (channel != incoming){
                                                            channel.writeAndFlush("[" + incoming.remoteAddress() + "]" + s + "\n");
                                                        } else {
                                                            channel.writeAndFlush("[you]" + s + "\n");
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (7)
                                                    Channel incoming = ctx.channel();
                                                    logger.info("SimpleChatClient:"+incoming.remoteAddress()+"异常");
                                                    // 当出现异常就关闭连接
                                                    cause.printStackTrace();
                                                    ctx.close();
                                                }
                                            });
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(ChannelOption.SO_REUSEADDR, true)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);
                    ChannelFuture future = bootstrap.bind(8080).sync();
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        }).start();
    }
}
