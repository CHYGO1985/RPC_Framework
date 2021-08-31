package com.chygo.rpc.api;

import com.chygo.rpc.pojo.User;

/**
 * The interface of User Service
 *
 * @author jingjiejiang
 * @history Aug 18, 2021
 *
 */
public interface UserService {

    /**
     * Get user by ID.
     *
     * @param id
     * @return
     */
    User getById(int id);

    /**
     *
     * Search user by ID and return status as string
     *
     * @param id
     * @return
     */
    String getByIdReturnStr(int id);
}
