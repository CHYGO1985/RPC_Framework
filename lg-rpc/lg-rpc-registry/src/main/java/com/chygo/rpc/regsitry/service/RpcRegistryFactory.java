package com.chygo.rpc.regsitry.service;

import com.chygo.rpc.api.RpcRegistryHandler;
import com.chygo.rpc.config.ConfigKeeper;
import com.chygo.rpc.regsitry.handler.ZookeeperRegistryHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.stereotype.Service;

/**
 *
 * Register center factory method
 *
 * @author jingjiejiang
 * @history Aug 24, 2021
 *
 */
@Service
public class RpcRegistryFactory implements FactoryBean<RpcRegistryHandler>, DisposableBean {

    private RpcRegistryHandler rpcRegistryHandler;

    @Override
    public void destroy() throws Exception {

        if (null != rpcRegistryHandler) {
            rpcRegistryHandler.destroy();
        }
    }

    @Override
    public RpcRegistryHandler getObject() throws Exception {

        if (null != rpcRegistryHandler) {
            return rpcRegistryHandler;
        }

        rpcRegistryHandler = new ZookeeperRegistryHandler(ConfigKeeper.getInstance().getZooKeeperAddr());
        return rpcRegistryHandler;
    }

    @Override
    public Class<?> getObjectType() {
        return RpcRegistryHandler.class;
    }
}
