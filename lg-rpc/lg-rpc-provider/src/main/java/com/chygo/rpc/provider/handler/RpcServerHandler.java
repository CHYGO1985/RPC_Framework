package com.chygo.rpc.provider.handler;

import com.chygo.rpc.annotation.RpcService;
import com.chygo.rpc.pojo.RpcRequest;
import com.chygo.rpc.pojo.RpcResponse;
import com.chygo.rpc.service.HeartBeat;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.beans.BeansException;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Rpc service handler.
 *
 * 1. Put @RpcService into bean cache
 * 2. Receive request from client side
 * 3. According to beanName and find the correspond bean in cache
 * 4. Interpret method name, param types and parameters info from request message
 * 5. Invoke bean method via reflection
 * 6. Send response to client
 *
 * @author jingjiejiang
 * @history Aug 15, 2021
 * 1. add channelRead method for object msg.
 * 2. add handler met
 *
 * Aug 18, 2021
 * 1. revise channelRead method for constructing result of RpsResponse as string
 *
 */
@Component
@ChannelHandler.Sharable
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> implements ApplicationContextAware {

    private static final Map SERVICE_INSTANCE_MAP = new ConcurrentHashMap();

    /**
     *
     * Put @RpcService into bean cache
     *
     * @param applicationContext
     * @throws BeansException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        Map<String, Object> serviceMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (serviceMap != null && serviceMap.size() > 0) {
            Set<Map.Entry<String, Object>> entries = serviceMap.entrySet();
            for (Map.Entry<String, Object> item : entries) {
                Object serviceBean = item.getValue();
                if (serviceBean.getClass().getInterfaces().length == 0) {
                    throw new RuntimeException("The service must implement an interface.");
                }

                // Get the first interface as the bean name by default
                String name = serviceBean.getClass().getInterfaces()[0].getName();
                SERVICE_INSTANCE_MAP.put(name, serviceBean);
            }
        }
    }

    /**
     *
     * Channel ready for reading event
     *
     * @param channelHandlerContext
     * @param rpcRequest
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {

        // if there is no new request, and the request ID equals to the old one in heartbeat
        if (HeartBeat.BEAT_ID.equalsIgnoreCase(rpcRequest.getRequestId())) {
            System.out.println("==== Server Idle ====");
            return ;
        }

        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(rpcRequest.getRequestId());
        try {
            rpcResponse.setResult(handler(rpcRequest));
        } catch (Exception e) {
            e.printStackTrace();
            rpcResponse.setError(e.getMessage());
            throw e;
        }

        channelHandlerContext.writeAndFlush(rpcResponse);

//        rpcResponse.setResult((Object) "success");
        ChannelFuture cf = channelHandlerContext.writeAndFlush(rpcResponse);
        // this is for checking if the reason of writing fails
        if (!cf.isSuccess()) {
            System.out.println("Send failed: " + cf.cause());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable) {

        System.out.println("Exception: " + throwable.getMessage());
        context.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object obj) throws Exception {

        if (obj instanceof IdleStateEvent) {
            context.channel().close();
        } else {
            super.userEventTriggered(context, obj);
        }
    }

    /**
     *
     * Business logic.
     *
     * @return
     */
    public Object handler(RpcRequest rpcRequest) throws InvocationTargetException {

        // 3. According to beanName and find the correspond bean in cache
        Object serviceBean = SERVICE_INSTANCE_MAP.get(rpcRequest.getClassName());
        if (serviceBean == null) {
            throw new RuntimeException("Cannot find the service via provided beanName, beanName:"
                    + rpcRequest.getClassName());
        }
        // 4. Interpret method name, param types and parameters info from request message
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = rpcRequest.getMethodName();
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] parameters = rpcRequest.getParameters();
        // 5. Invoke bean method via reflection (via CGLIB)
        FastClass fastClass = FastClass.create(serviceBeanClass);
        FastMethod method = fastClass.getMethod(methodName, parameterTypes);

        return method.invoke(serviceBean, parameters);
    }
}
