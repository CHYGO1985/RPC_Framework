package com.chygo.rpc.provider;

import com.chygo.rpc.config.ConfigKeeper;
import com.chygo.rpc.util.Util;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 *  Main method for server
 *
 * @author jingjiejiang
 * @history Aug 19, 2021
 *
 * 1. Aug 24, 2021
 * change the bootstrap method to connect to ZooKeeper
 *
 */
@ComponentScan(value = "com.chygo")
@SpringBootApplication
public class ServerBootstrapApplication {

    private static final int DEF_REPORT_INTERVAL = 5;

    public static void main(String[] args) {

        int port = Util.DEF_SERVER_PORT;
        if (args.length > 0 && NumberUtils.isDigits(args[0])) {
            port = Integer.parseInt(args[0]);
        }

        ConfigKeeper configKeeper = ConfigKeeper.getInstance();
        configKeeper.setPort(port);
        configKeeper.setZooKeeperAddr(Util.ZOOKEEPER_ADDRESS);
        configKeeper.setServerEnd(true);

        SpringApplication.run(ServerBootstrapApplication.class, args);
    }

    // for starting RPC server (as impl of CommandLineRunner)
//    @Override
//    public void run(String... args) throws Exception {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                rpcServer.startServer(Util.SERVER_IP, Util.SERVER_PORT_NUM);
//            }
//        }).start();
//    }
}
