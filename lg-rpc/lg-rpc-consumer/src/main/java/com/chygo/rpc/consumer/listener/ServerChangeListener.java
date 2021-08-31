package com.chygo.rpc.consumer.listener;

import com.chygo.rpc.api.NodeChangeListener;
import com.chygo.rpc.api.RpcRegistryHandler;
import com.chygo.rpc.consumer.client.RpcClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 *
 * The server change listener for removing services from list when a server is offline.
 *
 * @author jingjiejiang
 * @history Aug 25, 2021
 *
 */
public class ServerChangeListener implements NodeChangeListener {

    private static final int IP_IDX = 0;
    private static final int PORT_NUM_IDX = 1;

    public static Map<String, List<RpcClient>> SERVICE_CLIENT_MAP = new ConcurrentHashMap<>();
    private RpcRegistryHandler rpcRegistryHandler;
    private Map<String, Object> serviceInstanceMap;

    /**
     *
     * Init <service interface name : client list> map.
     * service interface name: com.lagou.edu.rpc.api.UserService
     *
     * @param rpcRegistryHandler
     * @param serviceInstanceMap
     */
    public ServerChangeListener(RpcRegistryHandler rpcRegistryHandler, Map<String, Object> serviceInstanceMap) {

        this.rpcRegistryHandler = rpcRegistryHandler;
        this.serviceInstanceMap = serviceInstanceMap;

        // Automatically register client to service interface
        serviceInstanceMap.entrySet().forEach(new Consumer<Map.Entry<String, Object>>() {

            @Override
            public void accept(Map.Entry<String, Object> entry) {

                // interface name: com.chygo.rpc.api.UserService
                String svcsInterfaceName = entry.getKey();

                try {
                    buildSvcsClientMap(svcsInterfaceName);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        rpcRegistryHandler.addListener(this);
    }

    /**
     *
     * Build <service : client objects> map.
     *
     * @param svcsInterfaceName
     * @throws InterruptedException
     *
     */
    private void buildSvcsClientMap(String svcsInterfaceName) throws InterruptedException {

        // 127.0.0.1:8991
        List<String> svrAddrList = rpcRegistryHandler.discover(svcsInterfaceName);

        List<RpcClient> rpcClients = SERVICE_CLIENT_MAP.get(svcsInterfaceName);
        if (CollectionUtils.isEmpty(rpcClients)) {
            rpcClients = new ArrayList<>();
        }

        for (String addr : svrAddrList) {
            // 0: ip, 1: port num
            String[] addrElems = addr.split(":");
            RpcClient rpcClient = new RpcClient(addrElems[IP_IDX], Integer.parseInt(addrElems[PORT_NUM_IDX]));
            rpcClient.initClient(svcsInterfaceName);

            rpcClients.add(rpcClient);
            SERVICE_CLIENT_MAP.put(svcsInterfaceName, rpcClients);
        }
    }

    public static Map<String, List<RpcClient>> getServiceClientMap() {
        return SERVICE_CLIENT_MAP;
    }

    /**
     *
     * Node change handling event.
     *
     * @param serviceName
     * @param serviceList
     * @param pathChildrenCacheEvent
     */
    @Override
    public void notify(String serviceName, List<String> serviceList, PathChildrenCacheEvent pathChildrenCacheEvent) {

        // get the current clients that connect to the server
        List<RpcClient> rpcClients = SERVICE_CLIENT_MAP.get(serviceName);
        PathChildrenCacheEvent.Type eventType = pathChildrenCacheEvent.getType();
        System.out.println("Server change event received: " + eventType + "----" + rpcClients
            + "----" + serviceName + "----" + serviceList);

        // path: /lg-rpc/com.chygo.rpc.api.UserService/provider/127.0.0.1:8990
        String path = pathChildrenCacheEvent.getData().getPath();
        String svrAddr = path.substring(path.lastIndexOf("/") + 1);
        String[] addrElems = svrAddr.split(":");

        // if the event type is to add new nodes in ZooKeeper and ge the server addr and add to SERVICE_CLIENT_MAP
        if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(eventType)
            || PathChildrenCacheEvent.Type.CONNECTION_RECONNECTED.equals(eventType)) {

            addClientToService(serviceName, rpcClients, addrElems);
            System.out.println("New added Node: " + svrAddr);
        } else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(eventType)
                || PathChildrenCacheEvent.Type.CONNECTION_SUSPENDED.equals(eventType)
                || PathChildrenCacheEvent.Type.CONNECTION_LOST.equals(eventType)) {

            removeClientFromService(svrAddr, rpcClients, addrElems);
        }
    }

    /**
     *
     * Add a client with specified ip and port to <service : clients> map.
     *
     * @param serviceName
     * @param rpcClients
     * @param addrElems specified ip and port
     */
    private void addClientToService(String serviceName, List<RpcClient> rpcClients, String[] addrElems) {

        if (CollectionUtils.isEmpty(rpcClients)) {
            rpcClients = new ArrayList<>();
        }

        RpcClient client = new RpcClient(addrElems[IP_IDX], Integer.parseInt(addrElems[PORT_NUM_IDX]));
        try {
            client.initClient(serviceName);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rpcClients.add(client);
    }

    /**
     *
     * Remove a client with specified ip and port from <service : clients> map.
     *
     * @param svrAddr
     * @param rpcClients
     * @param addrElems specified ip and port
     */
    private void removeClientFromService(String svrAddr, List<RpcClient> rpcClients, String[] addrElems) {

        // remove node
        if (CollectionUtils.isNotEmpty(rpcClients)) {

            for (int idx = 0; idx < rpcClients.size(); idx ++) {
                RpcClient client = rpcClients.get(idx);
                if (client.getIp().equalsIgnoreCase(addrElems[IP_IDX]) &&
                        client.getPort() == Integer.parseInt(addrElems[PORT_NUM_IDX])) {
                    rpcClients.remove(client);
                }

                System.out.println("Node removed: " + svrAddr);
            }
        }
    }
}
