package com.chygo.rpc.api;

import java.util.List;

/**
 *
 * Provide service register and detection.
 *
 * @author jingjiejiang
 * @history Aug 20, 2021
 *
 */
public interface RpcRegistryHandler {

    /**
     *
     * Register service.
     *
     * @param service
     * @param ip
     * @param port
     * @return
     */
    boolean register(String service, String ip, int port);

    /**
     *
     * Discover service.
     *
     * @param servcie
     * @return
     */
    List<String> discover(String servcie);

    /**
     *
     * Add listener.
     *
     * @param listener
     */
    void addListener(NodeChangeListener listener);

    /**
     *
     * Destory register instance.
     *
     */
    void destroy();
}
