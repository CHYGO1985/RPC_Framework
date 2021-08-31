package com.chygo.rpc.service;

import com.chygo.rpc.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 *
 * The encoder for RPC in JSON format.
 *
 * @author jingjiejiang
 * @history Aug 15, 2021
 *
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> targetClass;
    private Serializer serializer;

    public RpcDecoder(Class<?> targetClass, Serializer serializer) {

        this.targetClass = targetClass;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

        // As there will be an int type written into the buffer as the length, if total len < 4, then it is not an
        // available data
        if (byteBuf.readableBytes() < 4) {
            return ;
        }

        // Mark the position of readIndex
        byteBuf.markReaderIndex();

        // Read the length information, readInt() will shift readIndex for 4
        int dataLength = byteBuf.readInt();

        // if the length of message read is shorter than the set length, then resetReaderIndex
        // reset to the position where the markReaderIndex() method is called
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return ;
        }

        // read data into bytes[] array
        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);
        Object obj = serializer.deserialize(targetClass, data);
        list.add(obj);
    }
}
