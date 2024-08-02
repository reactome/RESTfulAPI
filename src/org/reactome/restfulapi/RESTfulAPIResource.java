package org.reactome.restfulapi;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.apache.log4j.Logger;
import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.reactome.psicquic.CustomizedInteractionService;
import org.reactome.psicquic.PSICQUICService;
import org.reactome.psicquic.model.QueryResults;
import org.reactome.psicquic.service.Service;
import org.reactome.restfulapi.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.*;


/**
 * Created Stanislav Palatnik.
 * Date: 6/9/11
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
    
    /**
     * Export SBML for an Event. 
     * @param dbId
     * @return
     */
    @GET
    @Path("/sbmlExporter/{dbId:\\d+}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sbmlExport(@PathParam("dbId") Long dbId) {
        String sbml = service.sbmlExport(dbId);
        
        if (sbml == null) { 
        	return buildResponse("Cannot generate SBML for " + dbId, null);
        }
        
        return buildResponse(sbml, dbId + ".sbml");
    }
    
    /**
     * Export SBGN for an Event.
     * @param dbId
     * @return
     */
    @GET
    @Path("/sbgnExporter/{dbId:\\d+}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response sbgnExport(@PathParam("dbId") long dbId) {
        String sbgn = service.sbgnExport(dbId);
        
        if (sbgn == null) { 
        	return buildResponse("Cannot generate SBGN for " + dbId, null);
        }
        
        return buildResponse(sbgn, dbId + ".sbgn");
    }
    
    private Response buildResponse(String responseEntity, String fileName) {
        ResponseBuilder responseBuilder = Response.status(Response.Status.OK).entity(responseEntity);
        
        if (fileName != null && !fileName.isEmpty()) {
            responseBuilder.header("Content-Disposition", "attachment;filename= " + fileName);
        }
        
        return responseBuilder.build();
    }
    
    /**
     * @param PathwayId The ID of the Pathway
     * @param format The format which the Pathway will be rendered in
     * @return Base64 encoded String of the pathway diagram
     */
    @GET
    @Path("/pathwayDiagram/{dbId : \\d+}/{format: .+}")
    @Produces(MediaType.TEXT_PLAIN)
    public String pathwayDiagram(@PathParam("dbId") final long PathwayId, 
                                 @PathParam("format") String format) {
        format = format.toLowerCase();
        return service.getPathwayDiagram(PathwayId, 
                                         format,
                                         null);
    }
    
    /**
     * Get the gene set in XML. This method is used by the Reactome R analysis package.
     * @return
     */
    @GET
    @Path("/GeneSetInXML")
    public String getGeneSetInXML() {
        return service.getGeneSetInXML();
    }
    
    /**
     * Highlight pathway diagrams using posted list of gene names.
     * @param pathwayId
     * @param format either PNG or PDF
     * @param query a list of gene names delimited by "," (no-space).
     * @return
     */
    @POST
    @Path("/highlightPathwayDiagram/{dbId}/{format}")
    @Produces(MediaType.TEXT_PLAIN)
    public String highlightPathwayDiagram(@PathParam("dbId") long pathwayId,
                                          @PathParam("format") String format,
                                          String query) {
        return service.getPathwayDiagram(pathwayId,
                                         format.toLowerCase(), 
                                         query.split(","));
    }
    
    // DEV-870 work starts here
    /**
     * Get the database identifier(s) for a Person by name
     * @return
     */
    @GET
    @Path("/queryPeopleByName/{name}")
    public List<DatabaseObject> getPersonsByName(@PathParam("name") String name) {
    	return service.getPeopleByName(name);
    }
    
    /**
     * Get the database identifier(s) for a Person by email
     * @return
     */
    @GET
    @Path("/queryPeopleByEmail/{email}")
    public List<DatabaseObject> getPersonssByEmail(@PathParam("email") String email) {
    	return service.getPeopleByEmail(email);
    }
    // DEV-870 work ends here
    // DEV-846 work starts here
    /**
     * Query a list of pathways that have been reviewed by a person.
     * @return List
     */
    @GET
    @Path("/queryReviewedPathways/{personId}")
    public List<Pathway> queryReviewedPathwaysJSON(@PathParam("personId") long personId) {
        return service.queryReviewedPathways(personId);
    }
    
    /**
     * Query a list of pathways that have been authored by a person.
     * @return List
     */
    @GET
    @Path("/queryAuthoredPathways/{personId}")
    public List<Pathway> queryAuthoredPathwaysJSON(@PathParam("personId") long personId) {
        return service.queryAuthoredPathways(personId);
    }

    // DEV-846 work ends here
    
    /**
     * Query a list of pathways that contain one or more genes in the query gene list.
     * @return
     */
    @POST
    @Path("/queryHitPathways")
    public List<Pathway> queryHitPathways(String queryGenes) {
        if (queryGenes == null || queryGenes.length() == 0)
            return new ArrayList<Pathway>();
        String[] genes = queryGenes.split(",");
        return service.queryHitPathways(genes);
    }
    
    /**
     * Get a list of maps from PhysicalEntity DB_IDs to their ReferenceEntity DB_IDs.
     * @return
     */
    @GET
    @Path("/getPhysicalToReferenceEntityMaps/{dbId}")
    public List<PhysicalToReferenceEntityMap> getPathwayPEToRefEntityMap(@PathParam("dbId") Long pathwayId) {
        return service.getPathwayPEToRefEntityMap(pathwayId);
    }
    
    /**
     * Get a list of maps from PhysicalEntity DB_IDs (directly in the diagram and contained in other PE) to their ReferenceEntity DB_IDs.
     * @return The PhysicalEntities participants and their associated ReferenceEntities for a given Event dbId 
     */
    @GET
    @Path("/getParticipantsToReferenceEntityMaps/{dbId}")
    public List<PhysicalToReferenceEntityMap> getPathwayParticipantsPEToRefEntityMap(@PathParam("dbId") String pathwayId) {
        return service.getPathwayParticipantPEToRefEntityMap(pathwayId);
    }

    /**
     * @param className Class Name of Object you are querying for
     * @param dbID
     * @return A full object of type className
     */
    @GET
    @Path("/queryById/{className}/{dbId}")
    public DatabaseObject queryById(@PathParam("className") final String className, @PathParam("dbId") final String dbID) {
        return service.queryById(className, dbID);
    }

    /**
     *
     * @param className Class Name of Object you are querying for
     * @param dbID
     * @param attribute
     * @return
     */
    @GET
    @Path("/queryById/{className}/{dbId}/{attribute}")
    @Produces(MediaType.TEXT_PLAIN)
    public String queryAttributeById(@PathParam("className") final String className,
                                     @PathParam("dbId") final String dbID,
                                     @PathParam("attribute") final String attribute) {
        String rtn = "";
        DatabaseObject dbOject = service.queryById(className, dbID);
        for (Method method : dbOject.getClass().getMethods()) {
            if (method.getName().toLowerCase().equals("get" + attribute.toLowerCase())){
                try {
                    if (method.getReturnType().equals(List.class)){
                        StringBuilder sb = new StringBuilder();
                        for (Object o : (List<?>) method.invoke(dbOject)) {
                            sb.append(o.toString()).append("\n");
                        }
                        if(sb.length()>0) {
                            rtn = sb.deleteCharAt(sb.length()-1).toString();
                        }
                    } else {
                        rtn = method.invoke(dbOject).toString();
                    }
                } catch (Exception e) { /* Nothing here */ }
            }
        }
        return rtn;
    }
    
    @GET
    @Path("/entitySubunits/{dbId}")
    public List<PhysicalEntity> getEntitySubunits(@PathParam("dbId") Long dbId) {
    	return getComplexSubunits(dbId);
    }
    
    /**
     * Get a list of complex subunits that are no-complex PEs.
     * @return
     */
    @GET
    @Path("/complexSubunits/{dbId}")
    public List<PhysicalEntity> getComplexSubunits(@PathParam("dbId") Long dbId) {
        return service.getComplexSubunits(dbId);
    }

    /**
     * @param className Class Name of Object you are querying for
     * @param post      Array of dbIDs
     * @return A list of full objects of type className
     */
    @POST
    @Path("/queryByIds/{className}")
    public List<DatabaseObject> queryByIds(@PathParam("className") String className,
                                           String post) {
        if (post.length() == 0)
            return null;
        // The first three characters should be "ID="
        post = post.substring(3);
        String[] dbIDs = post.split(",");
        List<String> ids = Arrays.asList(dbIDs);
        return service.queryByIds(className, ids);
    }

    /**
     * @param className Class Name of Object you are querying for
     * @param post      Array of dbIDs
     * @return A map of full objects of type className paring query and result (useful for the ST_ID transition time)
     */
    @POST
    @Path("/mapByIds/{className}")
    public Map<String, DatabaseObject> mapByIds(@PathParam("className") String className,
                                           String post) {
        if (post.length() == 0)
            return null;
        // The first three characters should be "ID="
        post = post.substring(3);
        String[] dbIDs = post.split(",");
        List<String> ids = Arrays.asList(dbIDs);
        return service.mapByIds(className, ids);
    }

    /**
     * @param className Class Name of Object you are querying for
     * @param  post Comma seperated list of Database ID's
     * @return list of of objects of type Class Name
     */
    @POST
    @Path("/listByQuery/{className}")
    public List<DatabaseObject> listByQuery(@PathParam("className") String className,
                                            String post) {
        //parse POST query for key field and key values
        StringTokenizer keyvalues = new StringTokenizer(post, "=");
        String propertyField = keyvalues.nextToken();
        String propertyValue = keyvalues.nextToken();
        return service.listByQuery(className, propertyField, propertyValue);
    }

    /**
     * Query an Event object based on name and a species. A pattern match will be performed by
     * this method. The species value can be empty.
     * @return
     */
    @GET
    @Path("/listByName/{className}/{containedName}/{species}")
    public List<DatabaseObject> listByNameAndSpecies(@PathParam("className") String className,
                                                     @PathParam("containedName") String containedName,
                                                     @PathParam("species") String species) {
        try {
            String name = URLDecoder.decode(containedName, "UTF-8");
            String speciesName = URLDecoder.decode(species, "UTF-8");
            if (speciesName.equalsIgnoreCase("null"))
                speciesName = null;
            return service.listByNameAndSpecies(className, name, speciesName);
        }
        catch(UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * This method is used to query an Event's species and its summation objects for a list
     * of Event ids by using a POST method.
     */
    @POST
    @Path("/queryEventSpeciesAndSummation")
    public List<Event> queryEventSpeciesAndSummation(String query) {
        String[] tokens = query.split(",");
        List<Long> dbIds = new ArrayList<Long>();
        for (String token : tokens)
            dbIds.add(new Long(token));
        try {
            List<Event> events = service.queryEventSpeciesAndSummation(dbIds);
            return events;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * @param PathwayId Pathway Id
     * @return List of PhysicalEntity instances that are present in an Event.
     */
    @GET
    @Path("/pathwayParticipants/{dbId : \\d+}")
    public List<PhysicalEntity> listPathwayParticipants(@PathParam("dbId") Long PathwayId) {
        List<PhysicalEntity> entities = service.listPathwayParticipants(PathwayId);
        return entities;
    }
    
    /**
     * @param PathwayId Pathway Id
     * @return List of Complex instances that are present in an Event.
     */
    @GET
    @Path("/pathwayComplexes/{dbId : \\d+}")
    public List<PhysicalEntity> listPathwayComplexes(@PathParam("dbId") Long PathwayId) {
        List<PhysicalEntity> entities = service.listPathwayComplexes(PathwayId);
        return entities;
    }

    /**
     * Get a list of DB_IDs for events contained by a Pathway with specified pathwayId. All events, both
     * Pathways and Reactions, should be in the returned list, recursively.
     * @param pathwayId DB_ID for a Pathway object.
     * @return a list of DB_IDs for Events contained by a Pathway object. The returned DB_IDs are in a
     * simple Text and delimited by ",".
     */
    @GET
    @Path("/getContainedEventIds/{dbId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getContainedEventIds(@PathParam("dbId") Long pathwayId) {
        List<Long> dbIds = service.getContainedEventIds(pathwayId);
        if (dbIds == null || dbIds.size() == 0)
            return ""; // Return an empty String to avoid null exception in the client.
        StringBuilder builder = new StringBuilder();
        for (Long dbId : dbIds) {
            builder.append(dbId).append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }

    /**
     * 
     * @return a list of reference molecules with their external IDs
     */
    
    @GET
    @Path("/getReferenceMolecules")
    @Produces(MediaType.TEXT_PLAIN)
    public String getReferenceMolecules() {
        List<String> dbIds = service.getReferenceMolecules();
        StringBuilder builder = new StringBuilder();
        for (String dbId : dbIds) {
            builder.append(dbId).append("\n");
        }
        return builder.toString();
    }
    
    
    @GET
    @Path("/getDiseases")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDiseases() {
        List<String> dbIds = service.getDiseases();
        StringBuilder builder = new StringBuilder();
        for (String dbId : dbIds) {
            builder.append(dbId).append("\n");
        }
        return builder.toString();
    }
    
    @GET
    @Path("/getUniProtRefSeqs")
    @Produces(MediaType.TEXT_PLAIN)
    public String getUniProtRefSeqs() {
        List<String> dbIds = service.getUniProtRefSeqs();
        StringBuilder builder = new StringBuilder();
        for (String dbId : dbIds) {
            builder.append(dbId).append("\n");
        }
        return builder.toString();
    }    
    
    
    
    @GET
    @Path("/topLevelPathways")
    public List<Pathway> listTopLevelPathways() {
        return service.listTopLevelPathways();
    }

    /**
     * @param post A comma separated list of Pathway ID's
     * @return List of Pathway Objects
     */
    @POST
    @Path("/pathwaysForEntities")
    public List<Pathway> queryPathwaysforEntities(String post) {
        if (post.length() == 0)
            return null;

        String entityIdsStr = post.substring(3);
        String[] entityIds = entityIdsStr.split(",");
        Long[] entityIdsList = new Long[entityIds.length];
        for (int i = 0; i < entityIds.length; i++) {
            entityIdsList[i] = Long.parseLong(entityIds[i]);
        }
        List<Long> IDs = Arrays.asList(entityIdsList);
        return service.queryPathwaysforEntities(IDs);
    }

    @GET
    @Path("/pathwaysWithDiagramForEntity/{dbId}")
    public List<Pathway> queryPathwaysWithDiagramForEntity(@PathParam("dbId") String dbId) {
        DatabaseObject entity = service.getDetailedView("DatabaseObject", dbId);
        if (entity instanceof PhysicalEntity) {
            return service.queryPathwaysWithDiagramForEntity(entity.getDbId());
        } else {
            return new ArrayList<Pathway>();
        }
    }
    
    /**
     * Get the detailed view for a DatabaseObject specified by its DB_ID.
     * @param dbId
     * @return
     */
    @GET
    @Path("/detailedView/{className}/{dbId}")
    public DatabaseObject getDetailedView(@PathParam("className") String className,
                                          @PathParam("dbId") String dbId) {
        DatabaseObject rtn = service.getDetailedView(className, dbId);
        return rtn;
    }

    /**
     * Returns the orthologous object for the specified species or null if not found
     * @param className  Class Name of Object you are querying for
     * @param identifier Identifiers of the object (could either be dbId or stId)
     * @param speciesId dbId of the species to find the orthologous
     * @return
     */
    @GET
    @Path("/orthologous/{className}/{identifier}/Species/{speciesId}")
    public DatabaseObject getOrthologous(@PathParam("className") String className,
                                         @PathParam("identifier") String identifier,
                                         @PathParam("speciesId") String speciesId){
        DatabaseObject rtn = service.getOrthologous(identifier, speciesId);
        return rtn;
    }

    /**
     * Returns the corresponding orthologous elements for a list passed by POST
     * @param speciesId The species identifier for the ortholgous
     * @param post      Array of dbIDs
     * @return A map of orthologs for those that have been found
     */
    @POST
    @Path("/orthologous/Species/{speciesId}")
    public Map<String, DatabaseObject> getorthologous(@PathParam("speciesId") String speciesId,
                                           String post) {
        if (post.length() == 0)
            return null;
        // The first three characters should be "ID="
        post = post.substring(3);
        String[] dbIDs = post.split(",");
        List<String> ids = Arrays.asList(dbIDs);

        Map<String, DatabaseObject> rtn = new HashMap<String, DatabaseObject>();
        for (String id : ids) {
            DatabaseObject orth = service.getOrthologous(id, speciesId);
            if(orth!=null) rtn.put(id, orth);
        }
        return rtn;
    }

    /**
     * Use this method to get a list of Pathways that have been listed in the 
     * FrontPage instance for other non-human species
     * @return
     */
    @GET
    @Path("/frontPageItems/{speciesName}")
    public List<Pathway> queryFrontPageItems(@PathParam("speciesName") String speciesName) {
        try {
            String decoded = URLDecoder.decode(speciesName, "utf-8");
            return service.listFrontPageItem(decoded);
        }
        catch(UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * Get a XML string for the pathway hierarchy for a specified species.
     * @param speciesName
     * @return
     */
    @GET
    @Path("/pathwayHierarchy/{speciesName}")
    public String getPathwayHierarchy(@PathParam("speciesName") String speciesName) {
        try {
            String decoded = URLDecoder.decode(speciesName, "utf-8");
            return service.generatePathwayHierarchy(decoded);
        }
        catch(UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * Query a list of LiteratureReference for a passed DB_ID for a Person instance.
     * @param personId
     * @return
     */
    @GET
    @Path("/queryReferences/{dbId}")
    public List<Publication> queryLiteratureReferenceForPerson(@PathParam("dbId") Long personId) { 
        return service.queryLiteratureReferenceForPerson(personId);
    }
    
    @GET
    @Path("/queryEventAncestors/{dbId}")
    public List<DatabaseObjectList> queryEventAncestors(@PathParam("dbId") Long eventId) {
        List<List<Event>> ancestors = service.queryAncestors(eventId);
        List<DatabaseObjectList> rtn = new ArrayList<DatabaseObjectList>();
        if (ancestors != null && ancestors.size() > 0) {
            for (List<Event> events : ancestors) {
                DatabaseObjectList wrapper = new DatabaseObjectList();
                wrapper.setDatabaseObject(events);
                rtn.add(wrapper);
            }
        }
        return rtn;
    }

    /**
     * This is the new API call for the pathway analysis for Reactome.
     * It finds the corresponding pathways from Reactome
     *
     * @param Post Array of Pathway Id's
     * @return Array of Pathway Objects encoded in a JSONArray or XML(client choice, depends on Accept header param)
     */
    @POST
    @Path("/pathwayMapping")
    public String doPathwayMapping(String Post) {
        Post = java.net.URLDecoder.decode(Post);
        String PathwayIdsStr = Post.substring(3);
        String[] PathwayIds = PathwayIdsStr.split(",");
        Long[] PathwayIdsList = new Long[PathwayIds.length];
        for (int i = 0; i < PathwayIds.length; i++) {
            PathwayIdsList[i] = Long.parseLong(PathwayIds[i]);
        }
        List<Long> IDs = Arrays.asList(PathwayIdsList);
        // Convert PathID's to an array of Path Ids, then pass it to the generator
        service.doPathwayMapping(IDs);
        return PathwayIdsStr;
    }

    /**
     * This is the new API call for the pathway analysis for Reactome.
     * Finds the Reactome pathways in which IDs in your list are strongly enriched
     *
     * @param Post Array of Pathway Id's
     * @return Array of Pathway Objects encoded in a either JSONArray or XML(client choice, depends on Accept header param)
     */
    @POST
    @Path("/pathwayEnrichmentAalysis")
    public String doPathwayEnrichmentAnalysis(String Post) {
        Post = java.net.URLDecoder.decode(Post);
        String PathwayIdsStr = Post.substring(3);
        String[] PathwayIds = PathwayIdsStr.split(",");
        Long[] PathwayIdsList = new Long[PathwayIds.length];
        for (int i = 0; i < PathwayIds.length; i++) {
            PathwayIdsList[i] = Long.parseLong(PathwayIds[i]);
        }
        List<Long> IDs = Arrays.asList(PathwayIdsList);
        // Convert PathID's to an array of Path Ids, then pass it to the generator
        service.doPathwayEnrichmentAnalysis(IDs);
        return PathwayIdsStr;
    }

    /**
     * Get a list of species that should be used in a pathway browser.
     * @return
     */
    @GET
    @Path("/speciesList")
    public List<Species> getSpeciesList() {
        return service.getSpeciesList();
    }
    
    /**
     * Get a list of PSICQUIC services registered at EBI.
     * @return
     */
    @GET
    @Path("/psicquicList")
    public List<Service> getPSIQUICServices() {
        // Initialized when needed in order to control memory usage
        PSICQUICService psicquicService = new PSICQUICService();
        return psicquicService.listPSIQUICSercices();
    }
    
    /**
     * Query for interactions.
     * @param dbId
     * @param serviceName
     * @return
     */
    @GET
    @Path("/psiquicInteractions/{dbId}/{service}")
    public QueryResults queryPSICQUICInteractions(@PathParam("dbId") Long dbId,
                                                  @PathParam("service") String serviceName) {
        PSICQUICService psicquicService = new PSICQUICService();
        psicquicService.setTempDir(service.getOutputdir());
        psicquicService.setMySQLAdaptor(service.getDba());
        QueryResults results = psicquicService.queryInteractions(dbId, serviceName);
        return results;
    }
    
    /**
     * Export a list of PSICQUIC interaction for a DB_ID in a simple text format.
     * @param dbId
     * @param serviceName
     * @return
     */
    @GET
    @Path("/exportPsiquicInteractions/{dbId}/{service}")
    @Produces(MediaType.TEXT_PLAIN)
    public String exportPSICQUICInteractions(@PathParam("dbId") Long dbId,
                                             @PathParam("service") String serviceName) {
        PSICQUICService psicquicService = new PSICQUICService();
        psicquicService.setTempDir(service.getOutputdir());
        psicquicService.setMySQLAdaptor(service.getDba());
        String text = psicquicService.exportInteractions(dbId, serviceName);
        return text;
    }
    
    /**
     * Upload a customized interaction file. The file should contain the following format:
     * UniProt1\tUniProt2
     * One interaction per line.
     * @return
     */
    @POST
    @Path("/uploadInteractionFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public String uploadInteractionFile(@FormDataParam("file") InputStream uploadIs,
                                        @FormDataParam("fileType") String fileType) throws IOException {
        // Though you may use FormDataContentDisposition to get some information about the uploaded
        // file, however, the file size value is -1, which is empty!
        try {
            CustomizedInteractionService ppiService = new CustomizedInteractionService();
            ppiService.setTempDir(service.getOutputdir());
            return ppiService.uploadInteractions(fileType,
                                                 uploadIs);
        }
        catch(IOException e) {
            logger.error(e.getMessage(), e);
            throw e; // Re-throw exception and hope to be popped-up by Jersey.
        }
    }
    
    /**
     * Submit a PSICQUIC URL for later use. The submitted URL will be kept in the server-side
     * for 24 hours only!
     * @param url // url should be the sole content posted from the client
     * @return
     */
    @POST
    @Path("/submitNewPSICQUIC")
    @Produces(MediaType.TEXT_PLAIN)
    public String submitNewPSICQUIC(String url) { 
        CustomizedInteractionService service = new CustomizedInteractionService();
        return service.registerUserPSICQUIC(url);
    }
    
    /**
     * Get a list of ReferenceEntity for an Event or PE defined by its DB_ID.
     * @param dbId
     * @return
     */
    @GET
    @Path("/referenceEntity/{dbId}")
    public List<ReferenceEntity> queryReferenceEntity(@PathParam("dbId") Long dbId) {
        List<ReferenceEntity> entities = service.getReferenceEntity(dbId);
        return entities;
    }
    
    /**
     * Returns the list of ALL LiteratureReferences stored in the database
     * @return
     * @throws Exception
     */
    @GET
    @Path("/literatureReferences")
    public List<LiteratureReference> queryAllLiteratureReferences() throws Exception {
        List<LiteratureReference> rtn = new ArrayList<LiteratureReference>();
        Collection<GKInstance> lrs = service.getDba().fetchInstancesByClass(ReactomeJavaConstants.LiteratureReference);
        for (GKInstance lr : lrs) {
            rtn.add((LiteratureReference) service.getConverter().convert(lr));
        }
        return rtn;
    }
}
