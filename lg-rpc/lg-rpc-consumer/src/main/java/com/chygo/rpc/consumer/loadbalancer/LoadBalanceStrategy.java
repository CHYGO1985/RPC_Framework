package com.chygo.rpc.consumer.loadbalancer;

import com.chygo.rpc.consumer.client.RpcClient;

import java.util.List;
import java.util.Map;

/**
 *
 * The interface for load balancer.
 *
 * @author jingjiejiang
 * @history Aug 24, 2021
 *
 */
public interface LoadBalanceStrategy {

    /**
     *
     * Get the client that subscribe the service.
     *
     * @param serviceClientMap key: com.lagou.edu.rpc.api.UserService value: RpcClient
     *                         The RpcClient has like server ip, port information
     * @param serviceClassName
     * @return
     */
    RpcClient route(Map<String, List<RpcClient>> serviceClientMap, String serviceClassName);
}
