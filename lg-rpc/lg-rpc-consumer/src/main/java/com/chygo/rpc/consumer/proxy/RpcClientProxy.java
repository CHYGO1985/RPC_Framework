package com.chygo.rpc.consumer.proxy;

import com.chygo.rpc.consumer.client.RpcClient;
import com.chygo.rpc.consumer.loadbalancer.LoadBalanceStrategy;
import com.chygo.rpc.consumer.loadbalancer.impl.RandomLoadBalancer;
import com.chygo.rpc.pojo.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 *
 * Client proxy class for creating proxy object.
 *
 * 1.Wrap request object
 * 2.Create RpcClient object
 * 3.Send message
 * 4.Get returned result
 *
 * @author jingjiejiang
 * @history Aug 15, 2021
 * 1. revised invoke() method, send as object not string.
 *
 * Aug 18, 2021
 * 1. revised invoke() method, to receive message as JSON.
 *
 * Aug 24, 2021
 * 1. add a new invoke() method for using loadbalancer
 *
 */
public class RpcClientProxy {
    
    private static LoadBalanceStrategy loadBalanceStrategy = new RandomLoadBalancer();
    // unit : ms
    private final static int THREAD_SLEEP_TIME = 5000;

    /**
     *
     * Create a new RpcRequest instance.
     *
     * @param method
     * @param args
     * @return
     */
    private static RpcRequest initRpcRequeset(Method method, Object[] args) {

        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);

        return rpcRequest;
    }

    /**
     *
     * Resend a RpcRequest.
     *
     * @param serviceClientMap
     * @param serviceClassName
     * @param rpcRequest
     * @return
     */
    private static Object resendRpcRequest(final Map<String, List<RpcClient>> serviceClientMap,
                                           String serviceClassName,
                                           RpcRequest rpcRequest) throws ExecutionException, InterruptedException {

        RpcClient otherRpcClient = loadBalanceStrategy.route(serviceClientMap,
                serviceClassName);
        if (null == otherRpcClient) {
            return null;
        }

        return otherRpcClient.sendRpcRequest(rpcRequest);
    }

    public static Object createProxy(final Class<?> serviceClass,
                                     final Map<String, List<RpcClient>> serviceClientMap) {

        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceClass}, new InvocationHandler() {

            /*
                    // Create a RpcClient to send and receive message
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        // 1.Wrap request object
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setClassName(method.getDeclaringClass().getName());
                        rpcRequest.setMethodName(method.getName());
                        rpcRequest.setParameterTypes(method.getParameterTypes());
                        rpcRequest.setParameters(args);
                        // 2.Create RpcClient object
                        RpcClient rpcClient = new RpcClient(Util.SERVER_IP,
                                Util.SERVER_PORT_NUM);
                        try {
                            // 3.Send message
                            Object responseMsg = rpcClient.send(rpcRequest);
//                            RpcResponse rpcResponse = JSON.parseObject(responseMsg.toString(), RpcResponse.class);
                            RpcResponse rpcResponse = (RpcResponse) responseMsg;
                            if (rpcResponse.getError() != null) {
                                throw new RuntimeException(rpcResponse.getError());
                            }
                            // 4.Get returned result
                            Object result = rpcResponse.getResult();

                            return result.toString();
                        } catch (Exception e) {
                            throw e;
                        } finally {
                            rpcClient.close();
                        }
                    }
                });
        */

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        // get Netty client
                        String serviceClassName = serviceClass.getName();

                        // wrap request object
                        RpcRequest rpcRequest = initRpcRequeset(method, args);
                        System.out.println("Request content: " + rpcRequest);

                        // get data from server end
                        RpcClient rpcClient = loadBalanceStrategy.route(serviceClientMap, serviceClassName);
                        if (null == rpcClient) {
                            return null;
                        }

                        try {
                            return rpcClient.sendRpcRequest(rpcRequest);
                        } catch (Exception e) {

                            if (e.getClass().getName().equals("java.nio.channels.ClosedChannelException")) {
                                System.out.println("Exception thrown when sending message : " + e.getMessage());
                                e.printStackTrace();
                                Thread.sleep(THREAD_SLEEP_TIME);
                                resendRpcRequest(serviceClientMap, serviceClassName, rpcRequest);
                            }

                            throw e;
                        }
                    }
                });
    }
}
