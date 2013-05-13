package org.reactome.restfulapi;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.reactome.psicquic.CustomizedInteractionService;
import org.reactome.psicquic.PSICQUICService;
import org.reactome.psicquic.model.QueryResults;
import org.reactome.psicquic.service.Service;
import org.reactome.restfulapi.details.pmolecules.converter.Converter;
import org.reactome.restfulapi.details.pmolecules.converter.Data2Excel;
import org.reactome.restfulapi.details.pmolecules.converter.ExportConfiguration;
import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;
import org.reactome.restfulapi.details.pmolecules.types.FormatType;
import org.reactome.restfulapi.details.pmolecules.types.QueryParams;
import org.reactome.restfulapi.models.DatabaseObject;
import org.reactome.restfulapi.models.DatabaseObjectList;
import org.reactome.restfulapi.models.Event;
import org.reactome.restfulapi.models.Pathway;
import org.reactome.restfulapi.models.PhysicalEntity;
import org.reactome.restfulapi.models.PhysicalToReferenceEntityMap;
import org.reactome.restfulapi.models.Publication;
import org.reactome.restfulapi.models.ReferenceEntity;
import org.reactome.restfulapi.models.Species;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;


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
     * Export SBML for an Event. 
     * @param dbId
     * @return
     */
    @GET
    @Path("/sbmlExporter/{dbId:\\d+}")
    @Produces(MediaType.TEXT_PLAIN)
    public String sbmlExport(@PathParam("dbId") Long dbId) {
        String sbml = service.sbmlExport(dbId);
        if (sbml == null)
            return "Cannot generate SBML for " + dbId;
        else
            return sbml;
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
     * @param  post Comma seperated list of Database ID's
     * @return list of of objects of type Class Name
     */
    @POST
    @Path("/listByQuery/{className}")
    public List<DatabaseObject> listByQuery(@PathParam("className") final String className,
                                            String post) {
        //parse POST query for key field and key values
        StringTokenizer keyvalues = new StringTokenizer(post, "=");
        String propertyField = keyvalues.nextToken();
        String propertyValue = keyvalues.nextToken();
        return service.listByQuery(className, propertyField, propertyValue);
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
     * Get a list of DB_IDs for events contained by a Pathway with specified pathwayId. All events, both
     * Pathways and Reactions, should be in the returned list, recursively.
     * @param pathwayId DB_ID for a Pathway object.
     * @return a list of DB_IDs for Events contained by a Pathway object. The returned DB_IDs are in a
     * simple Text and delimited by ",".
     */
    @GET
    @Path("/getContainedEventIds/{dbId}")
    public String getContainedEventIds(@PathParam("dbId") Long pathwayId) {
        List<Long> dbIds = service.getContainedEventIds(pathwayId);
        StringBuilder builder = new StringBuilder();
        for (Long dbId : dbIds) {
            builder.append(dbId).append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        return builder.toString();
    }

    @GET
    @Path("/topLevelPathways")
    public List<Pathway> listTopLevelPathways() {
        return service.listTopLevelPathways();
    }

    /**
     * @param post An comma seperated list of Pathway ID's
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
    
//    /**
//     * Use this method to get a list of Pathways that have been listed in the 
//     * FrontPage instance.
//     * @return
//     */
//    @GET
//    @Path("/frontPageItems")
//    public List<Pathway> queryFrontPageItems() {
//        // Default for human with null
//        return service.listFrontPageItem(null);
//    }
    
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
    
    @GET
    @Path("/participatingMolecules/{dbId}")
    public ResultContainer getParticipatingMolecules(@PathParam("dbId") final String dbID) {
    	Long id = Long.parseLong(dbID);
    	ResultContainer rc = service.getParticipatingMolecules(id);
    	return rc;
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

    @POST
    @Path("/participatingMolecules/export/{dbId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public String exportParticipatingMolecules(@PathParam("dbId") final String dbID, String post) {   	
    	post = java.net.URLDecoder.decode(post);
    	StringTokenizer keyvalues = new StringTokenizer(post, "=");
        String key = keyvalues.nextToken();
        String value = keyvalues.nextToken();
        
        QueryParams params;
    	try {
			params = new QueryParams(new JSONObject(value));
		} catch (JSONException e) {
			return null;
		}
    	
    	ExportConfiguration conf = new ExportConfiguration(params.getTypes(), params.getFields());
    	ResultContainer rc = getParticipatingMolecules(dbID);
    	Converter data = Converter.getConverter(params.getFormat(), rc, conf);
    	return data.getStringData();
    }
    
    @POST
    @Path("/participatingMolecules/download/{dbId}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    public Response downloadParticipatingMolecules(@PathParam("dbId") final String dbID, String post) {
    	post = java.net.URLDecoder.decode(post);
    	StringTokenizer keyvalues = new StringTokenizer(post, "=");
        String key = keyvalues.nextToken();
        String value = keyvalues.nextToken();
        
        QueryParams params;
    	try {
			params = new QueryParams(new JSONObject(value));
		} catch (JSONException e) {
			return null;
		}
    	
    	ExportConfiguration conf = new ExportConfiguration(params.getTypes(), params.getFields());
    	ResultContainer rc = getParticipatingMolecules(dbID);
    	Converter data = Converter.getConverter(params.getFormat(), rc, conf);
	
		Object rtnObject; String rtnType;
    	if(params.getFormat().equals(FormatType.EXCEL)){
    		HSSFWorkbook workbook = ((Data2Excel) data).getWorkbook();
    		rtnObject = (Object) workbook.getBytes();
			rtnType = "application/vnd.ms-excel";
		}else{
			rtnObject = (Object) data.getStringData();
			rtnType = "application/octet-stream";
		}
    	
    	String fileName = "data." + params.getFormat().getExtension();
    	ResponseBuilder builder = Response.ok(rtnObject);
    	builder.type(rtnType);
    	builder.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
		return builder.build();
    }
}