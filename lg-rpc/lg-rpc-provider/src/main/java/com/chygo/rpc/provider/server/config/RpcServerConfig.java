package com.chygo.rpc.provider.server.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 *
 * Server config class.
 *
 * @author jingjiejiang
 * @history Aug 24, 2021
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RpcServerConfig {

    /**
     * Application name
     */
    private String appName;

    /**
     * Server port num
     */
    private int port;

    /**
     * Server IP addr
     */
    private String ip;

    /**
     * Delay time to expose the server
     */
    private int delayTime;

    /**
     * Whether it is server
     */
    private boolean isServerEnd;

    /**
     * Service name and instance mapper.
     */
    private Map<String, Object> servicesMap;

    /**
     * Service list.
     */
    private List<String> stringList;
}
