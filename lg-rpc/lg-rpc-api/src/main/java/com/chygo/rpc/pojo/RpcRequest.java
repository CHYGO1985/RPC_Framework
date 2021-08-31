package com.chygo.rpc.pojo;

import lombok.Data;

/**
 *
 * The class for requests.
 *
 * @author jingjiejiang
 * @history Aug 18, 2021
 *
 */
@Data
public class RpcRequest {

    /**
     * The ID of request.
     */
    private String requestId;

    /**
     * The name of the class
     */
    private String className;

    /**
     * The method name.
     */
    private String methodName;

    /**
     * The parameters type.
     *
     */
    private Class<?>[] parameterTypes;

    /**
     * The parameters.
     *
     */
    private Object[] parameters;

}
