package com.chygo.rpc.provider.server;

import com.chygo.rpc.api.RpcRegistryHandler;
import com.chygo.rpc.config.ConfigKeeper;
import com.chygo.rpc.pojo.RpcRequest;
import com.chygo.rpc.pojo.RpcResponse;
import com.chygo.rpc.provider.handler.RpcServerHandler;
import com.chygo.rpc.provider.server.config.RpcServerConfig;
import com.chygo.rpc.regsitry.service.RpcRegistryFactory;
import com.chygo.rpc.regsitry.service.SvcsProviderLoader;
import com.chygo.rpc.serializer.JSONSerializer;
import com.chygo.rpc.service.HeartBeat;
import com.chygo.rpc.service.RpcDecoder;
import com.chygo.rpc.service.RpcEncoder;
import com.chygo.rpc.util.Util;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * The server class.
 *
 * @author jingjiejiang
 * @history Aug 15, 2021
 * 1. add decoder/encoder
 *
 * Aug 18, 2021
 * 1. Change encoder to JSON encoder.
 *
 */
@Service
public class RpcServer implements InitializingBean, DisposableBean {

    private static final String APP_NAME = "rpc-provider";
    private static final int DELAY_TIME = 3000;

    @Autowired
    private RpcRegistryFactory rpcRegistryFactory;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private RpcServerConfig rpcServerConfig;

    @Autowired
    RpcServerHandler rpcServerHandler;

    public void startServer() throws Exception {

        try {
            // 1. Create thread pool.
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            // 2. Creat server bootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            // 3. Config server
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            // Add encoder/decoder for String msg
//                            pipeline.addLast(new StringDecoder());
//                            pipeline.addLast(new StringEncoder());

                            // Add heartbeats test
                            pipeline.addLast(new IdleStateHandler(0, 0,
                                    HeartBeat.BEAT_INTERVAL * 3, TimeUnit.SECONDS));

                            // Add JSON Decoder/Encoder
                            pipeline.addLast(new RpcDecoder(RpcRequest.class, new JSONSerializer()));
                            pipeline.addLast(new RpcEncoder(RpcResponse.class, new JSONSerializer()));

                            // Business logic
                            pipeline.addLast(rpcServerHandler);
                        }
                    });

            String svrIp = Util.SERVER_IP;
            rpcServerConfig.setIp(svrIp);
            int svrPort = rpcServerConfig.getPort();
            String appName = rpcServerConfig.getAppName();

            // 4. Bind port
            ChannelFuture sync = serverBootstrap.bind(svrIp, svrPort).sync();

            // Delay register
            if (rpcServerConfig.getDelayTime() > 0) {
                Thread.sleep(rpcServerConfig.getDelayTime());
            }
            System.out.println("========== Server register begin  ==========");
            this.register(svrIp, svrPort, appName, rpcServerConfig.getServicesMap());
            System.out.println("========== Server is successfully launched. ip: "  + svrIp + ", port: "
                    + svrPort + " ==========");

            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    /**
     *
     * Close the rpc server.
     *
     */
    private void close() {

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     *
     * Register service via using registryHandler return from RegistryFactory.
     *
     * @param svrIp
     * @param svrPort
     * @param appName
     * @param servicesMap
     * @throws Exception
     */
    private void register(String svrIp, int svrPort, String appName, Map<String, Object> servicesMap) throws Exception {

        if (MapUtils.isEmpty(servicesMap)) {
            System.out.println("No service found");
            throw new RuntimeException("No service found");
        }

        RpcRegistryHandler rpcRegistryHandler = rpcRegistryFactory.getObject();
        if (null == rpcRegistryHandler) {
            System.out.println("RpcRegistryHandler is null");
            throw new RuntimeException("RpcRegistryHandler is null");
        }

        // SimpleResponseImpl, 127.0.0.1, 8990/8991
        servicesMap.entrySet().stream().forEach(stringObjectEntry -> rpcRegistryHandler.register(
                stringObjectEntry.getKey(), svrIp, svrPort
        ));
    }

    /**
     *
     * Close resources.
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }

    /**
     *
     * Start the rpc server.
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Map<String, Object> serviceMap = SvcsProviderLoader.getInstanceCacheMap();

        rpcServerConfig = RpcServerConfig.builder()
                .appName(APP_NAME)
                .port(ConfigKeeper.getConfigKeeper().getPort())
                .delayTime(DELAY_TIME)
                .servicesMap(serviceMap)
                .isServerEnd(true).build();

        startServer();
    }
}
