package com.chygo.rpc.consumer;

import com.chygo.rpc.api.RpcRegistryHandler;
import com.chygo.rpc.api.SimpleResponse;
import com.chygo.rpc.api.UserService;
import com.chygo.rpc.config.ConfigKeeper;
import com.chygo.rpc.consumer.listener.ServerChangeListener;
import com.chygo.rpc.consumer.proxy.RpcClientProxy;
import com.chygo.rpc.regsitry.handler.ZookeeperRegistryHandler;
import com.chygo.rpc.util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * For testing.
 *
 * @author jingjiejiang
 * @history Aug 18, 2021
 *
 */
public class ClientBootStrap {

    private static final int TIME_INTERVAL = 5;

    public static void main(String[] args) throws InterruptedException {

        Map<String, Object> serviceInstanceMap = new HashMap<>();
        serviceInstanceMap.put(UserService.class.getName(), UserService.class);
        serviceInstanceMap.put(SimpleResponse.class.getName(), SimpleResponse.class);

        ConfigKeeper.getInstance().setClintEnd(true);
        // launch a timely based thread pool, and report data every 5 seconds
        ConfigKeeper.getInstance().setInterval(TIME_INTERVAL);

        RpcRegistryHandler rpcRegistryHandler = new ZookeeperRegistryHandler(Util.ZOOKEEPER_ADDRESS);
        ServerChangeListener serverChangeListener = new ServerChangeListener(rpcRegistryHandler, serviceInstanceMap);

        SimpleResponse simpleResponse = (SimpleResponse) RpcClientProxy.createProxy(SimpleResponse.class,
                serverChangeListener.getServiceClientMap());

        while (true) {
            Thread.sleep(2000);
            try {
                simpleResponse.response("I am good!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // test UserService
//        User user = userService.getById(1);
//        String res = userService.getByIdReturnStr(1);
//        System.out.println(res);
    }
}
