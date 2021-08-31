package com.chygo.rpc.util;

/**
 *
 * The util class for constants and configurations.
 *
 * @author jingjiejiang
 * @history Aug 18, 2021
 *
 */
public class Util {

    /**
     *
     * Server end related constants for RPC.
     *
     */
    // Server IP : 127.0.0.1
    public static final String SERVER_IP = "127.0.0.1";

    // Server port num: 8899
    public static final int SERVER_PORT_NUM = 8899;

    /**
     *
     * Package path relate constants.
     *
     */
    // General package prefix
    public static final String PACKAGE_PREFIX = "com.chygo";

    // Package path delimiter
    public static final String PACKAGE_PATH_DELIMITER = ".";

    // Java complied class suffix
    public static final String JAVA_COMP_CLASS_SUFFIX = ".class";

    /**
     *
     * ZooKeeper ZNode Path related constants.
     *
     */
    // ZNode path: /lg-rpc/com.chygo.rpc.api.SimpleResponse/provider/127.0.0.1:8898
    public static final String ZNODE_PATH_PREFIX = "/lg-rpc/";
    public static final String ZNODE_PATH_DELIMITER = "/";
    public static final String UTF_8 = "UTF-8";

    /**
     *
     * ZooKeeper server config.
     *
     */
    public static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    /**
     *
     * Rpc server config
     *
     */
    public static final int DEF_SERVER_PORT = 8990;
}
