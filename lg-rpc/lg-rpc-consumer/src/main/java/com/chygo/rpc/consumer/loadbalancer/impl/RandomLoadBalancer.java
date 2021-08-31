package com.chygo.rpc.consumer.loadbalancer.impl;

import com.chygo.rpc.consumer.client.RpcClient;
import com.chygo.rpc.consumer.loadbalancer.AbstractLoadBalancer;

import java.util.List;
import java.util.Random;

/**
 *
 * The loadbalancer implementation based on random method.
 *
 * @author jingjiejiang
 * @history Aug 24, 2021
 *
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    /**
     *
     * The RpcClient has server ip and port, so it is to select a server.
     *
     * @param clientList
     * @return
     */
    @Override
    protected RpcClient doSelect(List<RpcClient> clientList) {

        Random random = new Random();

        return clientList.get(random.nextInt(clientList.size()));
    }
}
