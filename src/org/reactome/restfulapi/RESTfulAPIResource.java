package org.reactome.restfulapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.reactome.restfulapi.details.pmolecules.converter.Converter;
import org.reactome.restfulapi.details.pmolecules.converter.Data2Excel;
import org.reactome.restfulapi.details.pmolecules.converter.ExportConfiguration;
import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;
import org.reactome.restfulapi.details.pmolecules.types.FormatType;
import org.reactome.restfulapi.details.pmolecules.types.QueryParams;
import org.reactome.restfulapi.models.DatabaseObject;
import org.reactome.restfulapi.models.ListOfShellInstances;
import org.reactome.restfulapi.models.Pathway;
import org.reactome.restfulapi.models.PhysicalEntity;
import org.reactome.restfulapi.models.Publication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

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
     * @param className Class Name of Object you are querying for
     * @param Post      Array of dbIDs
     * @return A list of full objects of type className
     */
    @POST
    @Path("/queryByIds/{className}")
    public String queryByIds(@PathParam("className") String className, 
                             String Post, 
                             @HeaderParam("Accept") String accept) {
        Post = java.net.URLDecoder.decode(Post);
        Post = StringUtils.trimWhitespace(Post);
        if (Post.length() == 0)
            return null;
        Post = java.net.URLDecoder.decode(Post);
        Post = Post.substring(3);
        String[] dbIDs = Post.split(",");
        List<String> IDs = Arrays.asList(dbIDs);
        return service.queryByIds(className, IDs, accept);
    }

    /**
     * @param className Class Name of Object you are querying for
     * @param  Post Comma seperated list of Database ID's
     * @return list of of objects of type Class Name
     */
    @POST
    @Path("/listByQuery/{className}")
    public String listByQuery(@PathParam("className") final String className, String Post, @HeaderParam("accept") String accept) {
        //parse POST query for key field and key values
        Post = java.net.URLDecoder.decode(Post);
        StringTokenizer keyvalues = new StringTokenizer(Post, "=");
        String propertyField = keyvalues.nextToken();
        String propertyValue = keyvalues.nextToken();
        return service.listByQuery(className, propertyField, propertyValue, accept);
    }

    /**
     * @param PathwayId Pathway Id
     * @return List of PhysicalEntity ID’s that are present in a reaction
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
     * @param Post An comma seperated list of Pathway ID's
     * @return List of Pathway Objects
     */
    @POST
    @Path("/pathwaysForEntities")
    public List<Pathway> queryPathwaysforEntities(String Post) {
        Post = java.net.URLDecoder.decode(Post);
        Post = StringUtils.trimWhitespace(Post);
        if (Post.length() == 0)
            return null;

        String EntityIdsStr = Post.substring(3);
        String[] EntityIds = EntityIdsStr.split(",");
        Long[] EntityIdsList = new Long[EntityIds.length];
        for (int i = 0; i < EntityIds.length; i++) {
            EntityIdsList[i] = Long.parseLong(EntityIds[i]);
        }
        List<Long> IDs = Arrays.asList(EntityIdsList);
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
    
    /**
     * Use this method to get a list of Pathways that have been listed in the 
     * FrontPage instance.
     * @return
     */
    @GET
    @Path("/frontPageItems")
    public List<Pathway> queryFrontPageItems() {
        return service.listFrontPageItem();
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
    public List<ListOfShellInstances> queryEventAncestors(@PathParam("dbId") Long eventId) {
        return service.queryAncestors(eventId);
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