package com.xxl.mq.client.rpc.netcom.netty.codec;

import com.xxl.mq.client.rpc.serialize.HessianSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * encoder
 * @author xuxueli 2015-10-29 19:43:00
 */
public class NettyEncoder extends MessageToByteEncoder<Object> {

    private Class<?> genericClass;

    public NettyEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data = HessianSerializer.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}