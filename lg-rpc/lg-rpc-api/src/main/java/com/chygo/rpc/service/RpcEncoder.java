package com.chygo.rpc.service;

import com.chygo.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 *
 * The encoder to JSON format.
 *
 * @author jingjiejiang
 * @history Aug 15, 2021
 *
 */
public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> targetClass;
    private Serializer serializer;

    public RpcEncoder(Class<?> targetClass, Serializer serializer) {

        this.targetClass = targetClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {

        if (targetClass != null && targetClass.isInstance(o)) {
            byte[] bytes = serializer.serialize(o);
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
