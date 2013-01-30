/*
 * Created on Jan 30, 2013
 *
 */
package org.reactome.restfulapi;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.reactome.restfulapi.models.DatabaseObject;

import com.sun.jersey.core.impl.provider.entity.Inflector;

/**
 * This class is used to handle XML output for a list of DatabaseObject so that
 * class name may be used as XML element name. The implementation is based on this 
 * blog: https://blogs.oracle.com/PavelBucek/entry/returning_xml_representation_of_list.
 * @author gwu
 *
 */
@Produces(MediaType.APPLICATION_XML)
@Provider
public class DatabaseObjectListWriter implements MessageBodyWriter<List<? extends DatabaseObject>> {
    private final static Logger logger = Logger.getLogger(DatabaseObjectListWriter.class);
    // Cached all Marshallers to avoid multiple creation
    private Map<Class<?>, Marshaller> clsToMarshaller;
    
    public DatabaseObjectListWriter() {
        clsToMarshaller = new HashMap<Class<?>, Marshaller>();
    }
    
    private Marshaller getMarshaller(Class<?> cls) throws JAXBException {
        Marshaller m = clsToMarshaller.get(cls);
        if (m != null)
            return m;
        JAXBContext context = JAXBContext.newInstance(cls);
        m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_FRAGMENT, true);
        clsToMarshaller.put(cls, m);
        return m;
    }

    @Override
    public long getSize(List<? extends DatabaseObject> as, 
                        Class<?> type, 
                        Type genericType,
                        Annotation[] annotations, 
                        MediaType mediaType) {
      return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, 
                               Type genericType, 
                               Annotation[] annotations, 
                               MediaType mediaType) {
        if(mediaType.getSubtype().endsWith("xml") &&
           List.class.isAssignableFrom(type) &&
           genericType instanceof ParameterizedType) { //TODO: genericType may be needed to check to ensure they are subclass of DatabaseObject
            return true;
        }
        return false;
    }

    @Override
    public void writeTo(List<? extends DatabaseObject> list, 
                        Class<?> type, 
                        Type genericType, 
                        Annotation[] annotations, 
                        MediaType mediaType, 
                        MultivaluedMap<String, Object> httpHeaders, 
                        OutputStream entityStream) throws IOException, WebApplicationException {
        Charset c = Charset.forName("UTF-8");
        String cName = c.name();
        
        entityStream.write(String.format("<?xml version=\"1.0\" encoding=\"%s\" standalone=\"yes\"?>", cName).getBytes(cName));
        String clsName = getClassName(genericType);
        String wrapper = Inflector.getInstance().pluralize(clsName);
        entityStream.write(String.format("<%s>", wrapper).getBytes(cName));
        try {
            for (Object o : list) {
                Marshaller m = getMarshaller(o.getClass());
                if (m == null)
                    continue;
                m.marshal(o, entityStream);
            }
        }
        catch(JAXBException e) {
            logger.error(e.getMessage(), e);
        }
        entityStream.write(String.format("</%s>", wrapper).getBytes(cName));
    }
    
    /**
     * This helper class is used to find the class name from Type. This may not be reliable.
     * @param genericType
     * @return
     */
    private String getClassName(Type genericType) {
        String name = genericType.toString();
        if (name == null || name.length() == 0)
            return DatabaseObject.class.getSimpleName(); // Use the top-most name
        if (name.endsWith(">")) {
            int index = name.lastIndexOf(".");
            if (index > 0)
                return name.substring(index + 1, name.length() - 1);
        }
        return DatabaseObject.class.getSimpleName(); // Use as the default
    }
    
}
