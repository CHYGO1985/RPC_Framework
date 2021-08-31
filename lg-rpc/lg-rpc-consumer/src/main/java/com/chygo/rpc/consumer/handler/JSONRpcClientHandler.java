package com.chygo.rpc.consumer.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.Callable;

/**
 *
 * The client handler class.
 * 1.Send message
 * 2.Receive message
 *
 * @author jingjiejiang
 * @history Aug 15, 2021
 * 1. add requestObjMsg for send as an object.
 *
 * Aug 18, 2021
 * 1. add channelRead() method for receiving message as Object.
 *
 */
public class JSONRpcClientHandler extends SimpleChannelInboundHandler<String> implements Callable {

    private ChannelHandlerContext context;

    // Request message as String type (String channel)
    private String requestMsg;
    // Response message as String type (String channel)
    private String responseMsg;

    // Request message as Object (JSON channel)
    private Object requestObjMsg;
    // Response message as Object (JSON channel)
    private Object responseObjMsg;

    public void setRequestMsg(String requestMsg) {
        this.requestMsg = requestMsg;
    }

    public void setRequestObjMsg(Object requestObjMsg) {
        this.requestObjMsg = requestObjMsg;
    }

    /**
     *
     * Channel ready event
     *
     * @param ctx
     * @throws Exception
     *
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }

    /**
     *
     * Channel read ready event.
     *
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     *
     */
    @Override
    protected synchronized void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
        responseMsg = msg;
        //唤醒等待的线程
        notify();
    }

    /**
     *
     * Read object from the channel.
     *
     * @param channelHandlerContext
     * @param msg
     * @throws Exception
     */
    @Override
    public synchronized void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {

        responseObjMsg = msg;
        notify();
    }

    /**
     *
     * Send an object to server side.
     *
     */
    @Override
    public synchronized Object call() throws Exception {

        context.writeAndFlush(requestObjMsg);
        wait();
        return responseObjMsg;
    }
}
