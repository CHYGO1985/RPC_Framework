package com.chygo.rpc.consumer.handler;

import com.chygo.rpc.pojo.RpcResponse;
import com.chygo.rpc.service.HeartBeat;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

/**
 *
 * The client handler class.
 * 1.Send message
 * 2.Receive message
 *
 * @author jingjiejiang
 * @history Aug 26, 2021
 *
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                RpcResponse rpcResponse) throws Exception {

        System.out.println("Request ID: " + rpcResponse.getRequestId() +
                ", returned result: " + rpcResponse.getResult());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {

        if (event instanceof IdleStateEvent) {
            context.writeAndFlush(HeartBeat.BEAT_PING);
        } else {
            super.userEventTriggered(context, event);
        }
    }
}
