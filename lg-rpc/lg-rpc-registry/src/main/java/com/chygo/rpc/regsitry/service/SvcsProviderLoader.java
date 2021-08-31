package com.chygo.rpc.regsitry.service;

import com.chygo.rpc.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Read and store services imeplementation under "service" of "provider" in a mapper.
 *
 * @author jingjiejiang
 * @history Aug 23, 2021
 *
 */
public class SvcsProviderLoader {
    private static final Logger logger = LoggerFactory.getLogger(SvcsProviderLoader.class);

    // Store service provider instance
    private static Map<String, Object> instanceCacheMap = new ConcurrentHashMap<>();

    // Store the names of provide classes
    private static List<String> providerClassList = new ArrayList<>();

    static {
        loadSvcsProviderInstance(Util.PACKAGE_PREFIX);
    }

    private static void loadSvcsProviderInstance(String packageName) {

        findProviderClass(packageName);
        putProviderInstance(packageName);
    }

    /**
     *
     * Find all the service provider class (non bean class) and put them into providerClassList
     *
     * @param packageName like "rpc.provider"
     *
     */
    private static void findProviderClass(final String packageName) {

        // use the getRousrce methof of classLoader to get packagename and turn it into URL
        // url: .../lg-rpc-provider/target/classes/com/chygo
        URL url = new Object() {

            public URL getPath() {
                String packageDir = packageName.replace(Util.PACKAGE_PATH_DELIMITER,
                        FileSystems.getDefault().getSeparator());
                URL obj = this.getClass().getClassLoader().getResource(packageDir);
                return obj;
            }
        }.getPath();

        // transform url to file format, then check whether it is directory or real file
        // if it is directory, use recursive method to get the real file
        // if it is file, check if it is service provider(non-bean), if yes, then pyt into list
        File dir = new File(url.getFile());
        File[] fileArr = dir.listFiles();
        for (File file : fileArr) {

            if (file.isDirectory()) {
                findProviderClass(packageName + Util.PACKAGE_PATH_DELIMITER + file.getName());
            } else {
                // list item exmaple: com.chygo.rpc.provider.handler.RpcServerHandler
                providerClassList.add(packageName + Util.PACKAGE_PATH_DELIMITER +
                        file.getName().replace(Util.JAVA_COMP_CLASS_SUFFIX, ""));
            }
        }
    }

    /**
     *
     * Iterate the instance of all item in providerClassList, if it is an implementation of service, then use the
     * interface as key, implementation as value, put the pairs into isntanceCacheMap. It is part of the process
     * to register service.
     *
     * @param packageName
     *
     */
    private static void putProviderInstance(String packageName) {

        for (String providerClassName : providerClassList) {
            // generate the isntance of the class according to providerClassName (class path from src root)
            try {
                Class<?> svcsProviderClass = Class.forName(providerClassName);
                Class<?>[] interfaces = svcsProviderClass.getInterfaces();
                if (null == interfaces || interfaces.length <= 0) {
                    continue;
                }

                // Use the class path of the interface as key, as the consumer will use the class path to invoke the
                // instance via reflection
                // example: com.chygo.rpc.api.UserService
                String svcsProviderIntfClassPath = interfaces[0].getName();
                // if it is a bean, then continue
                if (!svcsProviderIntfClassPath.startsWith(packageName)) continue;

                // get the instance
                Object instance = svcsProviderClass.newInstance();
                instanceCacheMap.put(svcsProviderIntfClassPath, instance);
                logger.info("The service of " + svcsProviderIntfClassPath + " is registered.");
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, Object> getInstanceCacheMap() {
        return instanceCacheMap;
    }
}
