package com.chygo.rpc.api;

import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.List;

/**
 *
 * Node change listener.
 *
 * @author jingjiejiang
 * @history Aug 20, 2021
 *
 */
public interface NodeChangeListener {

    /**
     *
     * When a node is changed, notify listener.
     *
     * @param children
     * @param serviceList
     * @param pathChildrenCacheEvent
     */
    void notify(String children, List<String> serviceList, PathChildrenCacheEvent pathChildrenCacheEvent);
}
