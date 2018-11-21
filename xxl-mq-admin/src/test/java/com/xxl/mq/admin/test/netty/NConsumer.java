package com.xxl.mq.admin.test.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuxueli on 16/8/29.
 */
public class NConsumer {
    private static Logger logger = LoggerFactory.getLogger(NPruducer.class);

    static Channel channel;
    public static void main(String[] args) throws InterruptedException {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // init channel
                EventLoopGroup group = new NioEventLoopGroup();
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group).channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            public void initChannel(SocketChannel channel) throws Exception {
                                channel.pipeline()
                                        .addLast(new StringDecoder())
                                        .addLast(new StringEncoder())
                                        .addLast(new SimpleChannelInboundHandler() {

                                            @Override
                                            protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                                                System.out.println("接受消息:" + msg);
                                            }

                                        });
                            }
                        })
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_REUSEADDR, true)
                        .option(ChannelOption.SO_KEEPALIVE, true);
                try {
                    channel = bootstrap.connect("127.0.0.1", 8080).sync().channel();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
