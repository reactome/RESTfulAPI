package org.reactome.restfulapi;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * User: stan
 * Date: 6/26/11
 */
public class RESTfulApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(RESTfulAPIResource.class);
        return classes;
    }
}
