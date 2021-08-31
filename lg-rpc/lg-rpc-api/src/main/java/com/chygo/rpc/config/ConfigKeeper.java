package com.chygo.rpc.config;

/**
 *
 * To store global configuration.
 *
 * @author jingjiejiang
 * @history Aug 23, 2021
 *
 */
public class ConfigKeeper {

    private static volatile ConfigKeeper configKeeper;
    // Zookeeper port num
    private int port;
    // Zookeeper IP address and port num: "127.0.0.1:2181"
    private String zooKeeperAddr;
    // Report interval in the unit of second
    private int interval;
    // Client
    private boolean isClintEnd;
    // Server
    private boolean isServerEnd;

    private ConfigKeeper() {
    }

    /**
     *
     * Globally single instance.
     *
     * @return
     *
     */
    public static ConfigKeeper getInstance() {

        if (null == configKeeper) {
            synchronized (ConfigKeeper.class) {
                configKeeper = new ConfigKeeper();
            }
        }

        return configKeeper;
    }

    public static ConfigKeeper getConfigKeeper() {
        return configKeeper;
    }

    public static void setConfigKeeper(ConfigKeeper configKeeper) {
        ConfigKeeper.configKeeper = configKeeper;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getZooKeeperAddr() {
        return zooKeeperAddr;
    }

    public void setZooKeeperAddr(String zooKeeperAddr) {
        this.zooKeeperAddr = zooKeeperAddr;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public boolean isClintEnd() {
        return isClintEnd;
    }

    public void setClintEnd(boolean clintEnd) {
        this.isClintEnd = clintEnd;
        this.isServerEnd = !clintEnd;
    }

    public boolean isServerEnd() {
        return isServerEnd;
    }

    public void setServerEnd(boolean serverEnd) {
        this.isServerEnd = serverEnd;
        this.isClintEnd = !serverEnd;
    }
}
