package com.chygo.rpc.api;

/**
 *
 * The simple response interface for comm between server and client.
 *
 * @author jingjiejiang
 * @history Agu 23, 2021
 *
 */
public interface SimpleResponse {

    /**
     *
     * Simple response.
     *
     * @param word
     * @return
     */
    String response(String word);
}
