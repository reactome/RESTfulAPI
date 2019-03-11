package org.reactome.restfulapi;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sun.jersey.spi.resource.Singleton;


/**
 * Minimized RESTfulAPI used by www.reactome.org to provide BioPAX export and getDBName so that outside
 * users will not call other methods.
 */

@Path("/")
@Controller
@Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Singleton
public class RESTfulAPIResource {
    private static final Logger logger = Logger.getLogger(RESTfulAPIResource.class);
    @Autowired
    private APIControllerHelper service;

    /**
     * BioPAX exporter
     * @param dbId Pathway Id
     * @return BioPAX model in OWL format
     */
    @GET
    @Path("/biopaxExporter/{level}/{dbId : \\d+}")
    @Produces(MediaType.TEXT_PLAIN)
    public org.w3c.dom.Document bioPaxExporter(@PathParam("level") String level,
                                               @PathParam("dbId") long dbId) {
        return service.bioPaxExporter(level, dbId);
    }
    
    /**
     * Get the database name preconfigued in the RESTful API.
     * @return
     */
    @GET
    @Path("/getDBName")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDBName() {
    	return service.getDBName();
    }

    /**
     * Returns the release version where the RESTFul instance is pointing to
     * @return the release version where the RESTFul instance is pointing to
     */
    @GET
    @Path("/version")
    @Produces(MediaType.TEXT_PLAIN)
    public String getVersion() {
        try {
            return service.getDba().getReleaseNumber().toString();
        } catch (Exception e) {
            return null;
        }
    }

}
