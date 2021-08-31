package com.chygo.rpc.serializer;

import java.io.IOException;

/**
 *
 * The interface for serializing.
 *
 * @author jingjiejiang
 * @history Aug 15, 2021
 *
 */
public interface Serializer {

    /**
     *
     * Transform a Java object to binary.
     *
     * @param object
     * @return
     * @throws IOException
     */
    byte[] serialize(Object object) throws IOException;

    /**
     *
     * Turn binary to a Java object.
     *
     * @param bytes
     * @param <T>
     * @return
     * @throws IOException
     */
    <T> T deserialize(Class<T> targetClass, byte[] bytes) throws IOException;
}
