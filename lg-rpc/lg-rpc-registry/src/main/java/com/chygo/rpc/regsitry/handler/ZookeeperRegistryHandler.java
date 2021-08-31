package com.chygo.rpc.regsitry.handler;

import com.chygo.rpc.api.NodeChangeListener;
import com.chygo.rpc.api.RpcRegistryHandler;
import com.chygo.rpc.util.Util;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 *
 * The ZooKeeper based register center.
 * The ZNode path will be: /lg-rpc/com.chygo.rpc.api.SimpleResponse/provider/127.0.0.1:8898
 * 1) connect the server to ZooKeeper
 * 2) register services provided by the server to ZooKeeper
 * 3) discover services (client)
 *
 * @author jingjiejiang
 * @history Aug 23, 2021
 *
 */
public class ZookeeperRegistryHandler implements RpcRegistryHandler {

    // unit : ms
    private static final int TIME_OUT = 5000;
    private static final int RETRY_TIMES = 1;
    // unit : ms
    private static final int SLEEP_BTW_RETRY = 1000;

    private static final String PROVIDER_NAME = "provider";
    private static final String IP_PORT_NUM_CONNECTOR = ":";

    private static final List<NodeChangeListener> listenerList = new ArrayList<>();
    // ZooKeeper server url: 127.0.0.1:2181
    private final String url;
    private CuratorFramework zkClient;
    private volatile boolean closed;
    private List<String> serviceList = new CopyOnWriteArrayList<>();
    private static final ScheduledExecutorService REPORT_WORKER = Executors.newScheduledThreadPool(1);

    /**
     *
     * Connect the server to ZooKeeper.
     *
     * @param url
     */
    public ZookeeperRegistryHandler(String url) {

        this.url = url;

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(url)
                .retryPolicy(new RetryNTimes(RETRY_TIMES, SLEEP_BTW_RETRY))
                .connectionTimeoutMs(TIME_OUT)
                .sessionTimeoutMs(TIME_OUT);

        zkClient = builder.build();
        zkClient.getConnectionStateListenable().addListener((CuratorFramework curatorFramework,
                                                             ConnectionState connectionState) -> {
            if (ConnectionState.CONNECTED.equals(connectionState)) {
                System.out.println("The ZooKeeper register server is successfully connected.");
            }
        });

        zkClient.start();

        // Timely report for client
    }

    /**
     *
     * Register a server on ZooKeeper.
     * Register means create a ZNode which names include specified service, server ip and server port num.
     *
     * @param service : com.chygo.rpc.api.SimpleResponse
     * @param ip
     * @param port
     * @return
     */
    @Override
    public boolean register(String service, String ip, int port) {

        String zNodePathPrefix = genZNodePathPrefix(service);
        if (!isZNodePathExist(zNodePathPrefix)) {
            createZNodeWithoutData(zNodePathPrefix, false);
        }

        // full node path: /lg-rpc/com.chygo.api.UserService/provider/127.0.0.1:8990
        String zNodePath = zNodePathPrefix + Util.ZNODE_PATH_DELIMITER + ip + IP_PORT_NUM_CONNECTOR + port;
        createZNodeWithoutData(zNodePath, true);

        return true;
    }

    /**
     *
     * Find the address of providers and add them to service list.
     *
     * @param servcie
     * @return
     */
    @Override
    public List<String> discover(String servcie) {

        // parent node: /lg-rpc/com.chygo.api.UserService/provider
        String zNodePathPrefix = genZNodePathPrefix(servcie);

        try {
            // if the service list is empty, get it from zkClient, then update via watcher
//            if (Collection)
            if (CollectionUtils.isEmpty(serviceList)) {
                System.out.println("Get service address from register center ...");
                // sub node: 127.0.0.1:8991
                serviceList = zkClient.getChildren().forPath(zNodePathPrefix);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        this.registerWatcher(servcie, zNodePathPrefix);

        return serviceList;
    }

    /**
     *
     * Register watcher
     *
     * @param service
     * @param zNodePathPrefix
     */
    private void registerWatcher(final String service, final String zNodePathPrefix) {

        PathChildrenCache nodeCache = new PathChildrenCache(zkClient, zNodePathPrefix, true);

        try {
            nodeCache.getListenable().addListener((client, pathChildrenCacheEvent) -> {

                // update local cache
                serviceList = client.getChildren().forPath(zNodePathPrefix);
                listenerList.stream().forEach(nodeChangeListener -> {
                    System.out.println("Node changed");
                    nodeChangeListener.notify(service, serviceList, pathChildrenCacheEvent);
                });
            });

            // /lg-edu-rpc/com.lagou.edu.api.UserService/provider/127.0.0.1:8990
            /*
             * StartMode：
             * POST_INITIALIZED_EVENT: async init, it will trigger the event
             * NORMAL：async init
             * BUILD_INITIAL_CACHE：sync init
             *
             * */
            nodeCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addListener(NodeChangeListener listener) {
        listenerList.add(listener);
    }

    @Override
    public void destroy() {

        if (null != zkClient) {
            zkClient.close();
        }
    }

    /**
     *
     * Create a persistent ZNode without any data on ZooKeeper.
     *
     * @param zNodePath
     *
     */
    public void createPersistentZNodeWithoutData(String zNodePath) {

        try {
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zNodePath);
        } catch (KeeperException.NodeExistsException e) {
            throw new IllegalStateException("Path : " + zNodePath + " already exists.", e);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     *
     * Create an ephemeral ZNode without any data on ZooKeeper.
     *
     * @param zNodePath
     */
    public void createEphemeralZNodeWithoutData(String zNodePath) {

        try {
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(zNodePath);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     *
     * Create a persistent ZNode with data on ZooKeeper.
     *
     * @param zNodePath
     * @param zNodeData
     */
    protected void createPersistentZNodeWithData(String zNodePath, String zNodeData) {

        try {
            byte[] dataBytes = zNodeData.getBytes(Util.UTF_8);
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zNodePath, dataBytes);
        } catch (KeeperException.NodeExistsException e) {
        } catch (UnsupportedEncodingException e) {
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     *
     * Create a ephemeral ZNode with data on ZooKeeper.
     *
     * @param zNodePath
     * @param zNodeData
     */
    protected void createEhemeralZNodeWithData(String zNodePath, String zNodeData) {

        try {
            byte[] dataBytes = zNodeData.getBytes(Util.UTF_8);
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(zNodePath, dataBytes);
        } catch (UnsupportedEncodingException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     *
     * Update data of a ZNode with specified path.
     *
     * @param zNodePath
     * @param zNodeData
     */
    protected void updateZNodeData(String zNodePath, String zNodeData) {

        try {
            byte[] dataBytes = zNodeData.getBytes(Util.UTF_8);
            zkClient.setData().forPath(zNodePath, dataBytes);
        } catch (UnsupportedEncodingException e) {
        } catch (Exception exception) {
            throw new IllegalStateException(exception.getMessage(), exception);
        }
    }

    /**
     *
     * Create a ZNode on ZooKeeper without data on ZooKeeper.
     *
     * @param zNodePath
     * @param isEhemeral
     */
    public void createZNodeWithoutData(String zNodePath, boolean isEhemeral) {

        if (isEhemeral) {
            this.createEphemeralZNodeWithoutData(zNodePath);
        } else {
            this.createPersistentZNodeWithoutData(zNodePath);
        }
    }

    /**
     *
     * Create a ZNode with specified data on ZooKeeper.
     *
     * @param zNodePath
     * @param zNodeData
     * @param isEphemeral
     */
    public void createZNodeWithData(String zNodePath, String zNodeData, boolean isEphemeral) {

        if (isEphemeral) {
            this.createEhemeralZNodeWithData(zNodePath, zNodeData);
        } else {
            this.createPersistentZNodeWithData(zNodePath, zNodeData);
        }
    }

    /**
     *
     * Update a ZNode with specified data on ZooKeeper.
     *
     * @param zNodePath
     * @param zNodeData
     *
     */
    public void updateZNodeWithData(String zNodePath, String zNodeData) {

        try {
            byte[] dataBytes = zNodeData.getBytes(Util.UTF_8);
            zkClient.setData().forPath(zNodePath, dataBytes);
        } catch (UnsupportedEncodingException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     *
     * Update a ZNode with specified data with on ZooKeeper. If the ZNode does not exist, create one.
     *
     * @param zNodePath
     * @param zNodeData
     * @param isEphemeral
     */
    public void updateZNodeWithDataIfExist(String zNodePath, String zNodeData, boolean isEphemeral) {

        if (isEphemeral) {
            updateEphemeralZNode(zNodePath, zNodeData);
        } else {
            updatePersistentZNode(zNodePath, zNodeData);
        }
    }

    /**
     *
     * Check an ephemeral ZNode path exists or not on ZooKeeper.
     * If yes, update ZNode with specified data.
     * If no, create a ephemeral ZNode with specified data.
     *
     * @param zNodePath
     * @param zNodeData
     *
     */
    private void updateEphemeralZNode(String zNodePath, String zNodeData) {

        if (isZNodePathExist(zNodePath)) {
            this.updateZNodeWithData(zNodePath, zNodeData);
        } else {
            this.createEhemeralZNodeWithData(zNodePath, zNodeData);
        }
    }

    /**
     *
     * Check a persistent ZNode path exists or not on ZooKeeper.
     * If yes, update ZNode with specified data.
     * If no, create a ephemeral ZNode with specified data.
     *
     * @param zNodePath
     * @param zNodeData
     *
     */
    private void updatePersistentZNode(String zNodePath, String zNodeData) {

        if (isZNodePathExist(zNodePath)) {
            this.updateZNodeWithData(zNodePath, zNodeData);
        } else {
            this.createPersistentZNodeWithData(zNodePath, zNodeData);
        }
    }

    /**
     *
     * Check if a path of a ZNode exits or not on ZooKeeper.
     *
     * @return
     */
    public boolean isZNodePathExist(String zNodePath) {

        try {
            if (zkClient.checkExists().forPath(zNodePath) != null) {
                return true;
            }
        } catch (KeeperException.NoNodeException e) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }

        return false;
    }

    /**
     *
     * Generate ZNode path without server ip and port num: /lg-rpc/ + service + /provider
     * Example: parent path: /lg-rpc/com.chygo.rpc.api.SimpleResponse/provider/
     * Full path (parent path + children path): /lg-rpc/com.chygo.rpc.api.SimpleResponse/provider/127.0.0.1:8898
     *
     * @param service
     * @return
     */
    private String genZNodePathPrefix(String service) {
        return Util.ZNODE_PATH_PREFIX + service + Util.ZNODE_PATH_DELIMITER + PROVIDER_NAME;
    }

}
