package com.chygo.rpc.service;

import com.chygo.rpc.pojo.RpcRequest;

/**
 *
 * HeartBeat class.
 *
 * @author jingjiejiang
 * @history Aug 24, 2021
 *
 */
public final class HeartBeat {

    public static final int BEAT_INTERVAL = 3;
    public static final String BEAT_ID = "BEAT_PING_PONG";

    public static RpcRequest BEAT_PING;

    static {
        BEAT_PING = new RpcRequest();
        BEAT_PING.setRequestId(BEAT_ID);
    }
}
