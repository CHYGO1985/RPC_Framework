package com.chygo.rpc.pojo;

import lombok.Data;

/**
 *
 * The class for responses.
 *
 * @author jingjiejiang
 * @history Aug 18, 2021
 *
 */
@Data
public class RpcResponse {

    /**
     *
     * The response ID. (Same as request ID).
     *
     */
    private String requestId;

    /**
     *
     * The error message.
     *
     */
    private String error;

    /**
     *
     * The returned result.
     *
     */
    private Object result;

}