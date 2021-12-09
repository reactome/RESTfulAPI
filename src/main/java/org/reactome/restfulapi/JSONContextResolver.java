package org.reactome.restfulapi;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
@Provider
public class JSONContextResolver implements ContextResolver<ObjectMapper> {

    private ObjectMapper context;
//    private Class[] types = {DatabaseObject.class, 
//                             EntityWithAccessionedSequence.class,
//                             ResultContainer.class};

    public JSONContextResolver() throws Exception {
//        this.context = new JSONJAXBContext(JSONConfiguration.natural().humanReadableFormatting(true).build(), "org.reactome.restfulapi.models");
//                                           "org.reactome.restfulapi.models:" + // Delimit with ":"
//                                           "org.reactome.psicquic.model:" +
//                                           "org.reactome.restfulapi.details.pmolecules.model");
//        this.context = new JSONJAXBContext(JSONConfiguration.natural().build(),
//                                           types);
        this.context = new ObjectMapper();
        context.configure(org.codehaus.jackson.map.SerializationConfig.Feature.INDENT_OUTPUT, 
                          true);
        context.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES,
                          false);
    }

    public ObjectMapper getContext(Class<?> objectType) {
        return this.context;
//        for (Class type : types) {
//            if (type == objectType) {
//                return context;
//            }
//        }
//        return null;
    }
}