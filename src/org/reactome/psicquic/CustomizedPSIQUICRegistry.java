/*
 * Created on May 13, 2013
 *
 */
package org.reactome.psicquic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * This singleton class is used to regist a user submit PSICQUIC service. The user submitted
 * service will be kept for 24 hours since it was registered. After 24 hours it will be removed
 * automatically.
 * @author gwu
 *
 */
public class CustomizedPSIQUICRegistry {
    private final Logger logger = Logger.getLogger(CustomizedPSIQUICRegistry.class);
    private final long CLEAN_UP_TIME = 24 * 60 * 60000; // Keep for 24 hours!
    private static CustomizedPSIQUICRegistry registry;
    // Registered service
    private Map<String, String> nameToUrl;
    // Register time
    private Map<String, Long> nameToTime;
    
    public static CustomizedPSIQUICRegistry getRegistry() {
        if (registry == null)
            registry = new CustomizedPSIQUICRegistry();
        return registry;
    }
    
    /**
     * A private constructor to initialize a lower priority thread to clean-up
     * registration.
     */
    private CustomizedPSIQUICRegistry() {
        // Initialize a low priority thread to cleaned registered service
        Thread t = new Thread() {
            public void run() {
                while (true) {
                    cleanRegistry();
                    try {
                        Thread.sleep(CLEAN_UP_TIME);
                    }
                    catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        };
        t.setPriority(Thread.currentThread().getPriority() - 2);
        t.start();
    }
    
    /**
     * A utility method to clean-up registry.
     */
    private void cleanRegistry() {
        if (nameToUrl == null || nameToUrl.size() == 0)
            return; // Nothing to do!
        List<String> toBeRemoved = new ArrayList<String>();
        long currentTime = System.currentTimeMillis();
        for (String name : nameToUrl.keySet()) {
            Long registryTime = nameToTime.get(name);
            Long time = currentTime - registryTime;
            if (time >= CLEAN_UP_TIME) 
                toBeRemoved.add(name);
        }
        logger.info("Delete registed services: " + toBeRemoved);
        nameToUrl.keySet().removeAll(toBeRemoved);
        nameToTime.keySet().removeAll(toBeRemoved);
    }
    
    /**
     * Register a PSIQUIC server with a unique name and its URL.
     * @param serviceName
     * @param url
     */
    public void registry(String serviceName,
                         String url) {
        if (nameToUrl == null) {
            nameToUrl = new HashMap<String, String>();
            nameToTime = new HashMap<String, Long>();
        }
        if (nameToUrl.containsKey(serviceName))
            throw new IllegalArgumentException(serviceName + " has been registered before!");
        nameToUrl.put(serviceName, url);
        nameToTime.put(serviceName, System.currentTimeMillis());
    }
    
    /**
     * Get a registered PSICQUIC url for a passed service name. Null may be returned
     * if the name is not registed before or has expired.
     * @param serviceName
     * @return
     */
    public String getRegistedPSICQUICUrl(String serviceName) {
        if (nameToUrl != null)
            return nameToUrl.get(serviceName);
        return null;
    }
    
}
