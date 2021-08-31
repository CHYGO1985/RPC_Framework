package com.chygo.rpc.consumer.loadbalancer;

import com.chygo.rpc.consumer.client.RpcClient;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;

/**
 *
 * The abstract load balancer class.
 *
 * @author jingjiejiang
 * @history Aug 24, 2021
 *
 */
public abstract class AbstractLoadBalancer implements LoadBalanceStrategy{

    @Override
    public RpcClient route(Map<String, List<RpcClient>> serviceClientMap, String serviceClassName) {

        if (MapUtils.isEmpty(serviceClientMap)) {
            return null;
        }

        List<RpcClient> clientList = serviceClientMap.get(serviceClassName);
        if (null == clientList) {
            return null;
        }

        return doSelect(clientList);
    }

    protected abstract RpcClient doSelect(List<RpcClient> clientList);
}
