package org.reactome.restfulapi;

import com.sun.jersey.spi.resource.Singleton;
import org.apache.log4j.Logger;
import org.gk.graphEditor.PathwayEditor;
import org.gk.model.GKInstance;
import org.gk.model.InstanceUtilities;
import org.gk.model.ReactomeJavaConstants;
import org.gk.pathwaylayout.DiagramGeneratorFromDB;
import org.gk.pathwaylayout.PathwayDiagramGeneratorViaAT;
import org.gk.pathwaylayout.PathwayDiagramXMLGenerator;
import org.gk.persistence.DiagramGKBReader;
import org.gk.persistence.MySQLAdaptor;
import org.gk.persistence.MySQLAdaptor.QueryRequest;
import org.gk.render.Renderable;
import org.gk.render.RenderablePathway;
import org.gk.sbml.SBMLAndLayoutBuilderFields;
import org.gk.schema.InvalidAttributeException;
import org.gk.schema.SchemaAttribute;
import org.gk.schema.SchemaClass;
import org.gk.util.FileUtilities;
import org.gk.util.SwingImageCreator;
import org.jdom.output.DOMOutputter;
import org.reactome.biopax.ReactomeToBioPAX3XMLConverter;
import org.reactome.biopax.ReactomeToBioPAXXMLConverter;
import org.reactome.restfulapi.models.*;
import org.reactome.restfulapi.models.Event;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.List;

@Singleton
@SuppressWarnings({"unchecked", "rawtypes"})
public class APIControllerHelper {
    private Logger logger = Logger.getLogger(APIControllerHelper.class);
    private MySQLAdaptor dba;
    private ReactomeToRESTfulAPIConverter converter;
    private QueryHelper queryHelper;
    private String outputdir;
    // cache release number
    private Integer releaseNumber;

    public APIControllerHelper() {
    }
    
    public String getOutputdir() {
        return outputdir;
    }

    public void setOutputdir(String outputdir) {
        this.outputdir = outputdir;
    }

    private Integer getReleaseNumber() throws Exception {
        if (releaseNumber != null)
            return releaseNumber;
        releaseNumber = dba.getReleaseNumber();
        return releaseNumber;
    }

    public void setConverter(ReactomeToRESTfulAPIConverter converter) {
        this.converter = converter;
        queryHelper.setConverter(converter);
    }

    public void setDba(MySQLAdaptor dba) {
        this.dba = dba;
        // In order to keep dba connection valid.
        dba.initDumbThreadForConnection();
        queryHelper.setMySQLAdaptor(dba);
    }
    
    public MySQLAdaptor getDba() {
        return this.dba;
    }
    
    public String getDBName() {
    	if (this.dba == null)
    		return null;
    	return this.dba.getDBName();
    }

    public void setQueryHelper(QueryHelper queryHelper) {
        this.queryHelper = queryHelper;
    }

    public ReactomeToRESTfulAPIConverter getConverter() {
        return this.converter;
    }

    /*
    @param long dbID The ID of the pathway diagram requested
     */
    public org.w3c.dom.Document bioPaxExporter(String level,
                                               long dbID) {
        if (dba == null) // dba has to be defined explicitly
            throw new IllegalStateException("BioPAXExporterService.getBioPAXModel(): " +
                    "No db adaptor defined!");
        org.w3c.dom.Document w3cDoc = null;
        try {
            GKInstance event = dba.fetchInstance(dbID);
            if (event == null)
                throw new InstanceNotFoundException(dbID);
            if (!event.getSchemClass().isa("Event")) {
                throw new IllegalStateException("The specified Instance is not an Event instance. Only Event instance can be exported.");
            }
            if (level.equalsIgnoreCase("Level2")) {
                ReactomeToBioPAXXMLConverter converter = new ReactomeToBioPAXXMLConverter();
                converter.setReactomeEvent(event);
                converter.convert();
                DOMOutputter domOutputter = new DOMOutputter();
                w3cDoc = domOutputter.output(converter.getBioPAXModel());
            }
            else { // default should be the level 3, the current version
                ReactomeToBioPAX3XMLConverter converter = new ReactomeToBioPAX3XMLConverter();
                converter.setReactomeEvent(event);
                converter.convert();
                DOMOutputter domOutputter = new DOMOutputter();
                w3cDoc = domOutputter.output(converter.getBioPAXModel());
            }
        } 
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return w3cDoc;
    }
    
    /**
     * Export a Pathway or a Reaction into SBML. Only an Event can be export.
     * Otherwise, an emtpty string will be returned.
     * @param dbID
     * @return
     */
    public String sbmlExport(Long dbID) {
        try {
            // Check if dbId is a Pathway or ReactionlikeEvent
            GKInstance inst = dba.fetchInstance(dbID);
            if (!inst.getSchemClass().isa(ReactomeJavaConstants.Event)) {
                logger.error(inst + " is not an Event in sbmlExport()!");
                return null;
            }
            SBMLAndLayoutBuilderFields sbmlAndLayoutBuilder = new SBMLAndLayoutBuilderFields();
            sbmlAndLayoutBuilder.getDatabaseConnectionHandler().setDatabaseAdaptor(dba);
            // Test for a pathway
            List<String> values = new ArrayList<String>();
            values.add(dbID.toString());
            String idName = null;
            if (inst.getSchemClass().isa(ReactomeJavaConstants.Pathway))
                idName = "id";
            else
                idName = "rid";
            sbmlAndLayoutBuilder.addField(idName, values);
            values = new ArrayList<String>();
            String layout = "SBGN"; // Always use SBGN layout
            values.add(layout);
            sbmlAndLayoutBuilder.addField("layout", values);
            
            sbmlAndLayoutBuilder.convertPathways();
            String result = sbmlAndLayoutBuilder.getSbmlBuilder().sbmlString();
            return result;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    public List<PhysicalToReferenceEntityMap> getPathwayPEToRefEntityMap(Long pathwayId) {
        try {
            GKInstance pathway = dba.fetchInstance(pathwayId);
            if (pathway == null)
                throw new InstanceNotFoundException(pathwayId);
            if (!pathway.getSchemClass().isa(ReactomeJavaConstants.Pathway))
                throw new IllegalArgumentException(pathway + " is not a pathway!");
            Set<GKInstance> pes = InstanceUtilities.grepPathwayParticipants(pathway);
            List<PhysicalToReferenceEntityMap> maps = new ArrayList<PhysicalToReferenceEntityMap>();
            for (GKInstance pe : pes) {
//            	System.out.println("Check " + pe + "...");
                Set<GKInstance> refEntities = InstanceUtilities.grepReferenceEntitiesForPE(pe);
                if (refEntities == null || refEntities.size() == 0)
                    continue;
                PhysicalToReferenceEntityMap map = new PhysicalToReferenceEntityMap();
                List<ReferenceEntity> refs = new ArrayList<ReferenceEntity>();
                for (GKInstance ref : refEntities) {
                	DatabaseObject databaseObj = converter.createObject(ref);
                	if (databaseObj instanceof ReferenceEntity)
                		refs.add((ReferenceEntity)databaseObj);
                }
//                List<Long> refDbIds = new ArrayList<Long>();
//                for (GKInstance refEntity : refEntities) {
//                    refDbIds.add(refEntity.getDBID());
//                }
                map.setPeDbId(pe.getDBID());
                map.setRefEntities(refs);
//                map.setRefDbIds(refDbIds);
                maps.add(map);
            }
            return maps;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<PhysicalToReferenceEntityMap>();
    }
    
    public List<PhysicalToReferenceEntityMap> getPathwayParticipantPEToRefEntityMap(Long pathwayId){
    	Set<GKInstance> pes = new HashSet<GKInstance>(); 
    	try{
	    	GKInstance pathway = dba.fetchInstance(pathwayId);
	        if (pathway == null)
	            throw new InstanceNotFoundException(pathwayId);
	        if (!pathway.getSchemClass().isa(ReactomeJavaConstants.Pathway))
	            throw new IllegalArgumentException(pathway + " is not a pathway!");
	        Set<GKInstance> aux = InstanceUtilities.grepPathwayParticipants(pathway);
            pes.addAll(aux);
	        List<PhysicalToReferenceEntityMap> maps = new ArrayList<PhysicalToReferenceEntityMap>();
            for (GKInstance pe : pes) {
                Set<GKInstance> refEntities = InstanceUtilities.grepReferenceEntitiesForPE(pe);
                if (refEntities == null || refEntities.size() == 0)
                    continue;
                PhysicalToReferenceEntityMap map = new PhysicalToReferenceEntityMap();
                List<ReferenceEntity> refs = new ArrayList<ReferenceEntity>();
                for (GKInstance ref : refEntities) {
                	DatabaseObject databaseObj = converter.createObject(ref);
                	if (databaseObj instanceof ReferenceEntity){
                		ReferenceEntity re = (ReferenceEntity) databaseObj;
                		try{
                			if(ref.getSchemClass().isValidAttribute(ReactomeJavaConstants.identifier)){
                				re.setIdentifier((String) ref.getAttributeValue(ReactomeJavaConstants.identifier));
                			}
                			converter.fillInDetails(ref, databaseObj);
                		}catch(Exception e){
                    		//Nothing here
                		}
                		refs.add(re);
                	}
                }
                map.setPeDbId(pe.getDBID());
                map.setDisplayName(pe.getDisplayName());
                map.setSchemaClass(pe.getSchemClass().getName());
                map.setRefEntities(refs);
                maps.add(map);
            }
            return maps;
    	}catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<PhysicalToReferenceEntityMap>();
    }
    
    /**
     * This method is used to query species and filled summation instances for a list of
     * event ids.
     * @param eventIds
     * @return
     * @throws Exception
     */
    public List<Event> queryEventSpeciesAndSummation(List<Long> eventIds) throws Exception {
        List<Event> events = new ArrayList<Event>();
        for (Long eventId : eventIds) {
            GKInstance event = dba.fetchInstance(eventId);
            if (event == null) {
                logger.error(eventId + " cannot be found!");
                continue;
            }
            if (!event.getSchemClass().isa(ReactomeJavaConstants.Event)) {
                logger.error(eventId + " is not an Event!");
                continue;
            }
            Event eventObj = (Event) converter.createObject(event);
            events.add(eventObj);
            // Attach species
            List<GKInstance> species = event.getAttributeValuesList(ReactomeJavaConstants.species);
            if (species != null && species.size() > 0) {
                List<DatabaseObject> dbObjList = convertInstanceList(species);
                List<Species> speciesList = new ArrayList<Species>(dbObjList.size());
                for (DatabaseObject dbObj : dbObjList) {
                    if (dbObj instanceof Species)
                        speciesList.add((Species)dbObj);
                }
                eventObj.setSpecies(speciesList);
            }
            // Attach summation
            List<GKInstance> summations = event.getAttributeValuesList(ReactomeJavaConstants.summation);
            if (summations != null && summations.size() > 0) {
                List<Summation> summationList = new ArrayList<Summation>();
                for (GKInstance summation : summations) {
                    Summation summationObj = (Summation) converter.convert(summation);
                    summationList.add(summationObj);
                }
                eventObj.setSummation(summationList);
            }
        }
        return events;
    }
    
    /**
     * Query a list of pathways containing one or more genes from the passed gene
     * array.
     * @param genes gene symbols
     * @return
     */
    public List<Pathway> queryHitPathways(String[] genes) {
        try {
            // Connect to the de-normalized database
            String connectionStr = "jdbc:mysql://" + dba.getDBHost() + ":" + dba.getDBPort() + "/" + dba.getDBName() + "_dn";
            //+ "?autoReconnect=true";
            Properties prop = new Properties();
            prop.setProperty("user", dba.getDBUser());
            prop.setProperty("password", dba.getDBPwd());
            Connection conn = DriverManager.getConnection(connectionStr, prop);
            String sql = "select ei.DB_ID from Event_2_indirectIdentifier ei, Event_2_species es, " +
            		     "Pathway p where ei.DB_ID = es.DB_ID AND es.species = 48887 AND " +
            		     "ei.DB_ID = p.DB_ID AND ei.indirectIdentifier in (";
            StringBuilder builder = new StringBuilder();
            for (String gene : genes) {
                builder.append("'").append(gene).append("',");
            }
            builder.deleteCharAt(builder.length() - 1); // Delete the last ","
            sql += builder.toString() + ")";
            Statement stat = conn.createStatement();
            ResultSet resultSet = stat.executeQuery(sql);
            // Get a list of Pathway ids containing genes
            builder.setLength(0);
            while (resultSet.next()) {
                builder.append(resultSet.getLong(1)).append(",");
            }
            if (builder.length() == 0) { // Nothing returned
                resultSet.close();
                stat.close();
                conn.close();
                return new ArrayList<Pathway>();
            }
            builder.deleteCharAt(builder.length() - 1);
            resultSet.close();
            // Select pathways that have PathwayDiagrams
            sql = "SELECT representedPathway FROM PathwayDiagram_2_representedPathway " +
            		"WHERE representedPathway IN (" + builder.toString() + ")";
            Connection dbaConn = dba.getConnection();
            Statement dbaStat = dbaConn.createStatement();
            resultSet = dbaStat.executeQuery(sql);
            Set<Long> pathwayIds = new HashSet<Long>();
            builder.setLength(0);
            while (resultSet.next()) {
                pathwayIds.add(resultSet.getLong(1));
                builder.append(resultSet.getLong(1)).append(",");
            }
            resultSet.close();
            dbaStat.close();
            builder.deleteCharAt(builder.length() - 1);
            // Want the get the lowest pathways that have pathway diagrams. These
            // pathways should provide most detailed information in diagrams.
            sql = "SELECT DB_ID, hasEveryComponent FROM Pathway_2_hasEveryComponent " +
            		"WHERE DB_ID IN (" + builder.toString() + ")";
            resultSet = stat.executeQuery(sql);
            Map<Long, Set<Long>> pathwayToSubs = new HashMap<Long, Set<Long>>();
            while (resultSet.next()) {
                Long pathwayId = resultSet.getLong(1);
                Long subPathwayId = resultSet.getLong(2);
                Set<Long> subIds = pathwayToSubs.get(pathwayId);
                if (subIds == null) {
                    subIds = new HashSet<Long>();
                    pathwayToSubs.put(pathwayId, subIds);
                }
                subIds.add(subPathwayId);
            }
            resultSet.close();
            stat.close();
            conn.close();
            // Only pick up pathways that don't have sub-pathway diagrams
            List<Pathway> rtn = new ArrayList<Pathway>();
            for (Long dbId : pathwayIds) {
                Set<Long> subIds = pathwayToSubs.get(dbId);
                if (subIds != null) {
                    // Check if there is any sub-pathway has PathwayDiagram
                    subIds.retainAll(pathwayIds);
                }
                if (subIds == null || subIds.size() == 0) {
                    GKInstance instance = dba.fetchInstance(dbId);
                    Pathway pathway = (Pathway) converter.createObject(instance);
                    rtn.add(pathway);
                }
            }
            return rtn;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<Pathway>();
    }
    
    /**
     * Generate an XML String for GeneSet in XML. This method is used by Reactome R analysis package.
     * @return
     */
    public String getGeneSetInXML() {
        GeneSetXMLExporter exporter = new GeneSetXMLExporter();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            exporter.exportToXML(dba, 
                                 new HashMap<String, String>(),
                                 bos);
            return bos.toString();
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            return e.getMessage();
        }
    }
    
    /**
     * Generating a pathway diagram needs to redraw it first, which is an expensive process.
     * Caching a drawn diagram in XML can save a lot of time. However, each release should have
     * its own cached folder for diagram updating. The old ones can be deleted during deployment.
     * @TODO: Need to figure out why the saved XML in the database cannot be used directly. It is 
     * true for disease pathways, diagrams are dynamically generated. But how about for normal diagrams?
     * Probably some update is needed in gk_central?
     * @param pathway
     * @param pathwayDigram
     * @return
     */
    private String getCachedPathwayDiagramXML(GKInstance pathway,
                                              GKInstance pathwayDigram) throws Exception {
        String dirName = outputdir + File.separator + getReleaseNumber(); 
        File dir = new File(dirName);
        if (!dir.exists())
            return null;
        String fileName = pathway.getDBID() + "_" + pathwayDigram.getDBID() + ".xml";
        File file = new File(dir, fileName);
        if (!file.exists())
            return null;
        FileUtilities fu = new FileUtilities();
        fu.setInput(file.getAbsolutePath());
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = fu.readLine()) != null)
            builder.append(line).append("\n");
        fu.close();
        return builder.toString();
    }
    
    /**
     * Cache a generated pathway diagram for future use in order to save time to re-generate it.
     * @param pathway
     * @param pathwayDiagram
     * @param xml
     * @throws Exception
     * @link getCachedPathwayDiagramXML(GKInstance, GKInstance)
     */
    private void cachPathwayDiagramXML(GKInstance pathway,
                                       GKInstance pathwayDiagram,
                                       String xml) throws Exception {
        String dirName = outputdir + File.separator + getReleaseNumber(); 
        File dir = new File(dirName);
        if (!dir.exists())
            dir.mkdir();
        String fileName = pathway.getDBID() + "_" + pathwayDiagram.getDBID() + ".xml";
        File file = new File(dir, fileName);
        FileUtilities fu = new FileUtilities();
        fu.setOutput(file.getAbsolutePath());
        fu.printLine(xml);
        fu.close();
    }
    
    /**
     * Get a PathwayDiagram encoded in Base64 string for PDF or PNG, or in XML.
     * @param pathwayId
     * @param type
     * @param geneNames genes should be highlighted in the returned pathway diagram.
     * @return
     */
    public synchronized String getPathwayDiagram(long pathwayId, 
                                    String type,
                                    String[] geneNames) {
        String rtn = null;
        try {
            GKInstance pathway = dba.fetchInstance(pathwayId);
            if (pathway == null || !pathway.getSchemClass().isa(ReactomeJavaConstants.Pathway)) {
                logger.error("Pathway doesn't exist: " + pathwayId);
                throw new IllegalArgumentException("Pathway doesn't exist: " + pathwayId);
            }
            // Find PathwayDiagram
            Collection<?> c = dba.fetchInstanceByAttribute(ReactomeJavaConstants.PathwayDiagram,
                                                           ReactomeJavaConstants.representedPathway,
                                                           "=",
                                                           pathway);
            if (c == null || c.size() == 0) {
                //logger.error("Pathway diagram is not available for " + pathway.getDisplayName());
                throw new IllegalStateException("Pathway diagram is not available for " + pathway.getDisplayName());
            }

            GKInstance diagram = (GKInstance) c.iterator().next();
            if (type.equals("xml")) {
                if (dba.isUseCache()) {
                    String xml = getCachedPathwayDiagramXML(pathway, diagram);
                    if (xml != null)
                        return xml;
                }
                PathwayDiagramXMLGenerator xmlGenerator = new PathwayDiagramXMLGenerator();
                String xml = xmlGenerator.generateXMLForPathwayDiagram(diagram, pathway);
                if (dba.isUseCache())
                    cachPathwayDiagramXML(pathway, diagram, xml);
                return xml;
            }
            DiagramGKBReader reader = new DiagramGKBReader();
            reader.setPersistenceAdaptor(diagram.getDbAdaptor());
            RenderablePathway renderablePathway = reader.openDiagram(diagram);
            if (geneNames != null)
                highlightPathwayDiagram(renderablePathway, geneNames);
            PathwayEditor editor = new DiagramGeneratorFromDB().preparePathwayEditor(diagram, 
                                                                                     pathway, 
                                                                                     renderablePathway);
            // Just to make the tightNodes() work, have to do an extra paint
            // to make textBounds correct
            new PathwayDiagramGeneratorViaAT().paintOnImage(editor);
            editor.tightNodes(true);
            String fileName = diagram.getDisplayName();
            fileName = fileName.replaceAll("(\\\\|/)", "-");
            // Note: It seems there is a bug in the PDF exporter to set correct FontRenderContext.
            // Have to call PNG export first to make some rectangles correct.
            if (type.equals("png")) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedImage image = SwingImageCreator.createImage(editor);
                ImageIO.write(image, "png", baos);
                baos.flush();
                rtn = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(baos.toByteArray());
                baos.close();
            } else if (type.equals("pdf")) {
                String uuid = UUID.randomUUID().toString();
                // To avoid a long name that is not supported by a platform
                File pdfFileName = new File(outputdir, uuid + ".pdf");
                BufferedImage image = SwingImageCreator.createImage(editor);
                ImageIO.write(image, "png", pdfFileName);
                SwingImageCreator.exportImageInPDF(editor, pdfFileName);
                byte[] pdfBytes = org.apache.commons.io.FileUtils.readFileToByteArray(pdfFileName);
                rtn = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(pdfBytes);
                boolean result = pdfFileName.delete();
                if(result==false)
                {
                    throw new Exception("Pathway diagram file could not be deleted.");
                }
            } else {
                throw new Exception("Unsupported Media Type");
            }
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return rtn;
    }
    
    private void highlightPathwayDiagram(RenderablePathway diagram,
                                         String[] geneNames) throws Exception {
        List<Renderable> comps = diagram.getComponents();
        if (comps == null || comps.size() == 0 || geneNames == null || geneNames.length == 0)
            return;
        List<Long> dbIds = new ArrayList<Long>();
        for (Renderable r : comps) {
            if (r.getReactomeId() == null)
                continue;
            dbIds.add(r.getReactomeId());
        }
        List<Long> matched = InstanceUtilities.checkMatchEntityIds(dbIds, Arrays.asList(geneNames), dba);
        for (Renderable r : comps) {
            if (r.getReactomeId() == null)
                continue;
            if (matched.contains(r.getReactomeId())) {
//                r.setForegroundColor(Color.BLUE);
//                r.setLineColor(Color.BLUE);
                r.setBackgroundColor(Color.BLUE);
                r.setForegroundColor(Color.WHITE);
            }
        }
    }

    boolean isStableIdentifier(final String Id)
    {
        String StrId = String.valueOf(Id);
        boolean result = StrId.matches("REACT.*");
        return result;
    }
    
    public DatabaseObject queryById(String className, 
                                    String id) {
        GKInstance instance;
        DatabaseObject rtn = new DatabaseObject();
        try {
            rtn = fetchInstance(className, 
                                id);
        }
        catch (InstanceNotFoundException e) {
            logger.error("Cannot find instance of " + id + " in class " + className, e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return rtn;
    }

    /**
     * A helper method to fetch an instance based on either DB_ID or stable id.
     * @param className
     * @param id
     * @return
     * @throws Exception
     * @throws InstanceNotFoundException
     */
    private DatabaseObject fetchInstance(String className, String id) throws Exception, InstanceNotFoundException {
        GKInstance instance = null;
        DatabaseObject rtn = null;
        if (!isStableIdentifier(id)) {
            Long dbIdl = new Long(id);
            instance = dba.fetchInstance(className, dbIdl);
            if (instance == null) {
                throw new InstanceNotFoundException(dbIdl);
            }
            rtn = (DatabaseObject) converter.convert(instance);
        }
        else { // This should be stable id
            Collection col = dba.fetchInstanceByAttribute(ReactomeJavaConstants.StableIdentifier,
                                                          ReactomeJavaConstants.identifier, 
                                                          "=",
                                                          id);
            if (col == null || col.size() == 0) {
                throw new InstanceNotFoundException(ReactomeJavaConstants.StableIdentifier,
                                                    id);
            }
            Collection objCol = dba.fetchInstanceByAttribute(className, 
                                                             ReactomeJavaConstants.stableIdentifier,
                                                             "=", 
                                                             (GKInstance)col.iterator().next());
            if (objCol == null || objCol.size() == 0) {
                throw new InstanceNotFoundException(className, id);
            }
            // Choose the first returned object only
            rtn = (DatabaseObject) converter.convert((GKInstance)objCol.iterator().next());
        }
        return rtn;
    }
    
    /**
     * Get Complex subunits recursively.
     * @param dbId
     * @return
     */
    public List<PhysicalEntity> getComplexSubunits(Long dbId) {
        try {
            GKInstance complex = dba.fetchInstance(dbId);
            Set<GKInstance> components = InstanceUtilities.getContainedInstances(complex,
                                                                                 ReactomeJavaConstants.hasComponent,
                                                                                 ReactomeJavaConstants.hasMember,
                                                                                 ReactomeJavaConstants.hasCandidate);
            // Do a filter to remove complex instances
            for (Iterator<GKInstance> it = components.iterator(); it.hasNext();) {
                GKInstance comp = it.next();
                if (comp.getSchemClass().isa(ReactomeJavaConstants.Complex) ||
                    comp.getSchemClass().isa(ReactomeJavaConstants.EntitySet))
                    it.remove();
            }
            List<PhysicalEntity> rtn = new ArrayList<PhysicalEntity>(components.size());
            for (GKInstance comp : components) {
                PhysicalEntity pe = (PhysicalEntity) converter.createObject(comp);
                rtn.add(pe);
                // Export referenceEntity if available
                if (comp.getSchemClass().isValidAttribute(ReactomeJavaConstants.referenceEntity)) {
                    GKInstance refEnt = (GKInstance) comp.getAttributeValue(ReactomeJavaConstants.referenceEntity);
                    if (refEnt != null) {
                        ReferenceEntity refEntInst = (ReferenceEntity) converter.createObject(refEnt);
                        Method method = ReflectionUtility.getNamedMethod(pe, "setReferenceEntity");
                        if (method != null)
                            method.invoke(pe, refEntInst);
                    }
                }
            }
            return rtn;
        }
        catch(InstanceNotFoundException e) {
            logger.error("Cannot find instance for " + dbId, e);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<PhysicalEntity>();
    }

    public List<DatabaseObject> queryByIds(String className, 
                                           List<String> ids) {
        List<DatabaseObject> rtn = new ArrayList<DatabaseObject>();
        try {
            for (String id : ids) {
                DatabaseObject converted = fetchInstance(className, id);
                if (converted != null)
                    rtn.add(converted);
            }
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return rtn;
    }

    public List<DatabaseObject> listByQuery(String className, 
                                            String propertyName, 
                                            String propertyValue) {
        try {
            List<GKInstance> instances = queryHelper.query(className,
                                          propertyName,
                                          propertyValue);
            return convertInstanceList(instances);
        } 
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<DatabaseObject>();
    }

    private List<DatabaseObject> convertInstanceList(List<GKInstance> instances) throws Exception {
        List<DatabaseObject> rtn = new ArrayList<DatabaseObject>();
        for (GKInstance gkInstance : instances) {
            DatabaseObject converted = (DatabaseObject) converter.createObject(gkInstance);
            rtn.add(converted);
        }
        return rtn;
    }
    
    public List<DatabaseObject> listByNameAndSpecies(String className,
                                                     String containedName,
                                                     String species) {
        try {
            List<GKInstance> instances = queryHelper.queryByNameAndSpecies(className, 
                                                                           containedName, 
                                                                           species);
            return convertInstanceList(instances);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<DatabaseObject>();
    }
    
    /**
     * Get a list of ReferenceEntity for a PE or Event that is specified by its DB_ID.
     * @param dbId
     * @return
     */
    public List<ReferenceEntity> getReferenceEntity(Long dbId) {
        List<ReferenceEntity> rtn = new ArrayList<ReferenceEntity>();
        try {
            GKInstance inst = dba.fetchInstance(dbId);
            if (!inst.getSchemClass().isa(ReactomeJavaConstants.Event) && !inst.getSchemClass().isa(ReactomeJavaConstants.PhysicalEntity)) {
                logger.warn("Passed DB_ID " + dbId + " is not for an Event or PhysicaleEntity: " + inst);
                return rtn;
            }
            Set<GKInstance> refEntities = null;
            if (inst.getSchemClass().isa(ReactomeJavaConstants.Event)) {
                Set<GKInstance> pes = InstanceUtilities.grepPathwayParticipants(inst);
                refEntities = new HashSet<GKInstance>();
                for (GKInstance pe : pes) {
                    if (!pe.getSchemClass().isa(ReactomeJavaConstants.PhysicalEntity))
                        continue;
                    refEntities.addAll(InstanceUtilities.grepReferenceEntitiesForPE(pe));
                }
            }
            else // Treated as PE
                refEntities = InstanceUtilities.grepReferenceEntitiesForPE(inst);
            for (GKInstance ref : refEntities) {
                ReferenceEntity refObj = (ReferenceEntity) converter.createObject(ref);
                rtn.add(refObj);
            }
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return rtn;
    }
    
    /**
     * Get a list of DB_IDs for Events contained by a Pathway object specified by its DB_ID
     * or stable_Id. All events contained by the Pathway should be in the returned List recursively.
     * @return
     */
    public List<Long> getContainedEventIds(Long pathwayId) {
        List<Long> dbIds = new ArrayList<Long>();
        try {
            GKInstance pathway = dba.fetchInstance(pathwayId);
            if (pathway != null && pathway.getSchemClass().isa(ReactomeJavaConstants.Pathway)) {
                Set<GKInstance> containedEvents = InstanceUtilities.getContainedInstances(pathway, 
                                                                                          ReactomeJavaConstants.hasEvent);
                for (GKInstance event : containedEvents)
                    dbIds.add(event.getDBID());
            }
//            Collections.sort(dbIds);
        }
        catch(Exception e) {
            logger.error("Error in getContainedEventIds", e);
        }
        return dbIds;
    }

    public List<PhysicalEntity> listPathwayParticipants(Long pathwayId) {
        try {
            GKInstance pathway = dba.fetchInstance(pathwayId);
            if (pathway == null)
                throw new InstanceNotFoundException(ReactomeJavaConstants.Pathway,
                                                    pathwayId);
            return converter.listPathwayParticipants(pathway);
        }
        catch(InstanceNotFoundException e) {
            logger.error("Cannot find pathway " + pathwayId, e);
        }
        catch(Exception e) {
            logger.error("Error in listPathwayParticipants for " + pathwayId, e);
        }
        return new ArrayList<PhysicalEntity>();
    }
    
    public List<PhysicalEntity> listPathwayComplexes(Long pathwayId) {
        try {
            GKInstance pathway = dba.fetchInstance(pathwayId);
            if (pathway == null)
                throw new InstanceNotFoundException(ReactomeJavaConstants.Pathway,
                                                    pathwayId);
            List<PhysicalEntity> complexes = converter.listPathwayComplexes(pathway);
            List<PhysicalEntity> rtn = new ArrayList<PhysicalEntity>();
            // Append each complex
            for (PhysicalEntity complex : complexes) {
            	rtn.add(complex);
            	
            	List<PhysicalEntity> subunits = this.getComplexSubunits(complex.getDbId()); 
            	
            	// Then append each subunit (with referenceEntities) after the complex
            	for (PhysicalEntity subunit : subunits) {
            		rtn.add(subunit);
            	}
            }
            	
            return rtn;
        
        }
        catch(InstanceNotFoundException e) {
            logger.error("Cannot find pathway " + pathwayId, e);
        }
        catch(Exception e) {
            logger.error("Error in listPathwayParticipants for " + pathwayId, e);
        }
        return new ArrayList<PhysicalEntity>();
        
    }

    public List<Pathway> listTopLevelPathways() {
        List<QueryRequest> qr = new ArrayList<QueryRequest>();
        org.gk.schema.Schema schema = dba.getSchema();
        SchemaClass pathwayCls = schema.getClassByName("Pathway");
        SchemaAttribute hasCompAtt;
        try {
            hasCompAtt = pathwayCls.getAttribute("hasEvent");
            qr.add(dba.createReverseAttributeQueryRequest(pathwayCls, hasCompAtt,
                    "IS NULL", null));
        } catch (InvalidAttributeException e) {
            e.printStackTrace();
        }
        Collection topLevelPathways = null;
        try {
            topLevelPathways = dba.fetchInstance(qr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Sorting all pathways
        List sortedList = new ArrayList(topLevelPathways);
        InstanceUtilities.sortInstances(sortedList);
        List<Pathway> pathways = new ArrayList<Pathway>(topLevelPathways.size());
        for (Iterator it = sortedList.iterator(); it.hasNext(); ) {
            GKInstance instance = (GKInstance) it.next();
            Pathway pathway = new Pathway();
            try {
                pathway = (Pathway) converter.convert(instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
            pathways.add(pathway);
        }
        return pathways;
    }
    
    /**
     * Get the species list for pathways listed in the front page items and their
     * orthologous events.
     * @return
     */
    public List<Species> getSpeciesList() {
        try {
            Collection<?> c = dba.fetchInstancesByClass(ReactomeJavaConstants.FrontPage);
            if (c == null || c.size() == 0)
                return null;
            GKInstance frontPage = (GKInstance) c.iterator().next();
            List<GKInstance> values = frontPage.getAttributeValuesList(ReactomeJavaConstants.frontPageItem);
            Set<GKInstance> species = new HashSet<GKInstance>();
            for (GKInstance pathway : values) {
                List<GKInstance> speciesList = pathway.getAttributeValuesList(ReactomeJavaConstants.species);
                if (speciesList != null) 
                    species.addAll(speciesList);
                List<GKInstance> orEvents = pathway.getAttributeValuesList(ReactomeJavaConstants.orthologousEvent);
                if (orEvents == null)
                    continue;
                for (GKInstance orEvent : orEvents) {
                    speciesList = orEvent.getAttributeValuesList(ReactomeJavaConstants.species);
                    if (speciesList != null)
                        species.addAll(speciesList);
                }
            }
            // In case of not using cache in the server-side, using HashSet will create duplicated entries
            // So using a HashMap keyed by DB_IDs should avoid this problem
            Map<Long, GKInstance> dbIdToInst = new HashMap<Long, GKInstance>();
            for (GKInstance inst : species) {
            	dbIdToInst.put(inst.getDBID(), inst);
            }
            List<Species> rtn = new ArrayList<Species>();
            // Place the human in the top: this is special
            Species human = null;
            for (GKInstance s : dbIdToInst.values()) {
                Species converted = (Species) converter.createObject(s);
                if (s.getDBID().equals(48887L))
                    human = converted;
                else
                    rtn.add(converted);
            }
            Collections.sort(rtn, new Comparator<Species>() {
                public int compare(Species s1, Species s2) {
                    return s1.getDisplayName().compareTo(s2.getDisplayName());
                }
            });
            rtn.add(0, human);
            return rtn;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * Generate an XML encoding the pathway hierarchy for a specified species.
     * @param speciesName
     * @return
     */
    public String generatePathwayHierarchy(String speciesName) {
        try {
            List<GKInstance> pathways = getFrontPageItems(speciesName);
            if (pathways == null || pathways.size() == 0)
                return null;
            PathwayHierarchyGenerator helper = new PathwayHierarchyGenerator();
            return helper.generatePathwayHierarchy(pathways, speciesName);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    private List<GKInstance> getFrontPageItems(String speciesName) throws Exception {
        // Get the FrontPage instance. It is assumed that there should be only one
        // FrontPage instance
        Collection<?> c = dba.fetchInstancesByClass(ReactomeJavaConstants.FrontPage);
        if (c == null || c.size() == 0)
            return null;
        // Just in case
        if (speciesName == null || speciesName.equals(""))
            speciesName = "Homo sapiens"; //TODO: This may need to be set in an external configuration in the future!
        // Want to ignore cases
        speciesName = speciesName.toLowerCase();
        GKInstance frontPage = (GKInstance) c.iterator().next();
        List<?> values = frontPage.getAttributeValuesList(ReactomeJavaConstants.frontPageItem);
        List<GKInstance> rtnList = new ArrayList<GKInstance>();
        for (Iterator<?> it = values.iterator(); it.hasNext();) {
            GKInstance inst = (GKInstance) it.next();
            // In case for human or chicken pathways that are listed in the top-level
            // Use list in case multiple species are used (e.g. HIV with human)
            List<GKInstance> speciesList = inst.getAttributeValuesList(ReactomeJavaConstants.species);
            boolean hasFound = false;
            for (GKInstance species : speciesList) {
                if (species.getDisplayName().toLowerCase().equals(speciesName)) {
                    rtnList.add(inst);
                    hasFound = true;
                    break;
                }
            }
            if (hasFound)
                continue;
            // Check predicted pathways
            List<GKInstance> orEvents = inst.getAttributeValuesList(ReactomeJavaConstants.orthologousEvent);
            for (GKInstance orEvent : orEvents) {
                // For inferred pathway, only one species needs to be checked.
                GKInstance species = (GKInstance) orEvent.getAttributeValue(ReactomeJavaConstants.species);
                if (species.getDisplayName().toLowerCase().equals(speciesName)) {
                    rtnList.add(orEvent);
                    break;
                }
            }
        }
        return rtnList;
    }
    
    public synchronized List<Pathway> listFrontPageItem(String speciesName) {
        try {
            List<GKInstance> frontPageItems = getFrontPageItems(speciesName);
            if (frontPageItems == null)
                return null;
            List<Pathway> pathways = new ArrayList<Pathway>(frontPageItems.size());
            for (GKInstance inst : frontPageItems) {
                Pathway pathway = (Pathway) converter.convert(inst);
                pathways.add(pathway);
            }
            return pathways;
        }
        catch(Exception e) {
            logger.error("Cannot get FrontPage items", e);
        }
        return new ArrayList<Pathway>();
    }
    
    public List<Publication> queryLiteratureReferenceForPerson(Long personId) {
        try {
            GKInstance person = dba.fetchInstance(personId);
            if (person == null)
                return new ArrayList<Publication>();
            Collection<GKInstance> referrers = person.getReferers(ReactomeJavaConstants.author);
            List<Publication> references = new ArrayList<Publication>();
            for (GKInstance inst : referrers) {
                if (!inst.getSchemClass().isa("Publication"))
                    continue;
                Publication publication = (Publication) converter.convert(inst);
                references.add(publication);
            }
            return references;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<Publication>();
    }
    
    public List<List<Event>> queryAncestors(Long eventId) {
        try {
            GKInstance event = dba.fetchInstance(eventId);
            return queryHelper.queryAncestors(event);
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<List<Event>>();
    }
    
    /**
     * Get the detailed view for a DatabaseObject specified by its DB_ID.
     * @param dbId
     * @return
     */
    public DatabaseObject getDetailedView(String className,
                                          String dbId) {
        DatabaseObject rtn = queryById(className, dbId);
        if (rtn != null && rtn.getDbId() != null) {
            try {
                GKInstance instance = dba.fetchInstance(rtn.getDbId());
                converter.fillInDetails(instance, rtn);
            }
            catch(Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return rtn;
    }

    public List<Pathway> queryPathwaysforEntities(final List<Long> EntityIds) {
//      Convert entities to GKInstances so that database query can be done for performance reason
        Set<GKInstance> instances = new HashSet<GKInstance>();
        List<Pathway> rtn = null;
        GKInstance instance;
        try {
            for (long id : EntityIds) {
                instance = dba.fetchInstance(id);
                if (instance == null)
                    throw new InstanceNotFoundException(id);
                instances.add(instance);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (instances.size() == 0)
            return new ArrayList<Pathway>();
//      Query the list of Reactions containing instances
        // Have to consider all complexes those GKInstances participate too
        try {
            //TODO: Should we also check with EntitySet?
            Set<GKInstance> complexes = grepComplexesForEntities(instances);
            instances.addAll(complexes);
            Set<GKInstance> reactions = getParticipatingReactions(instances);
            rtn = grepTopPathwaysFromReactions(reactions);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return rtn;
    }

    public String doPathwayMapping(final List<Long> PathwayIds) {
        return "tba";
    }

    public String doPathwayEnrichmentAnalysis(final List<Long> PathwayIds) {
        return "tba";
    }

    /**
     * A helper method to get all complexes that use the passed entities as some of their
     * components.
     *
     * @param entities
     * @return
     * @throws Exception
     */
    private Set<GKInstance> grepComplexesForEntities(Set<GKInstance> entities)
            throws Exception {
        Set<GKInstance> complexes = new HashSet<GKInstance>();
        Collection query = new HashSet<GKInstance>(entities);
        while (true) {
            query = dba.fetchInstanceByAttribute(ReactomeJavaConstants.Complex,
                    ReactomeJavaConstants.hasComponent,
                    "=",
                    query);
            if (query != null && query.size() > 0)
                complexes.addAll(query);
            else
                break;
        }
        return complexes;
    }

    private List<Pathway> grepTopPathwaysFromReactions(Collection reactions) throws Exception {
        // Create the paths from the passed Pathways to the top level Pathways
        List<List<GKInstance>> paths = new ArrayList<List<GKInstance>>();
        for (Iterator it = reactions.iterator(); it.hasNext(); ) {
            GKInstance pathway = (GKInstance) it.next();
            List<List<GKInstance>> paths1 = getAncestorPaths(pathway);
            paths.addAll(paths1);
        }
        mergePaths(paths);
        // Now get the top level pathways
        Set<GKInstance> set = new HashSet<GKInstance>();
        for (List<GKInstance> path : paths) {
            // Only Pathways are needed
            for (int i = path.size() - 1; i >= 0; i--) {
                GKInstance bottom = path.get(i);
                if (bottom.getSchemClass().isa(ReactomeJavaConstants.Pathway)) {
                    set.add(bottom);
                    break;
                }
            }
        }
        List<GKInstance> topPathways = new ArrayList<GKInstance>();
        for (GKInstance instance : set) {
            topPathways.add(instance);
        }
        InstanceUtilities.sortInstances(topPathways);
        // Convert the topPathways to Pathway objects
        List<Pathway> rtn = new ArrayList<Pathway>();
        for (GKInstance pathwayInstance : topPathways) {
            Pathway pathway = (Pathway) converter.createObject(pathwayInstance);
            rtn.add(pathway);
        }
        return rtn;
    }

    private Set<GKInstance> getParticipatingReactions(Collection<GKInstance> entities) throws Exception {
        Set<GKInstance> reactions = new HashSet<GKInstance>();
        // Inputs
        Collection collection = dba.fetchInstanceByAttribute(ReactomeJavaConstants.ReactionlikeEvent,
                ReactomeJavaConstants.input,
                "=",
                entities);
        if (collection != null)
            reactions.addAll(collection);
        // Outputs
        collection = dba.fetchInstanceByAttribute(ReactomeJavaConstants.ReactionlikeEvent,
                ReactomeJavaConstants.output,
                "=",
                entities);
        if (collection != null)
            reactions.addAll(collection);
        // CatalystActivities
        // Get CatalystActivities first
        collection = dba.fetchInstanceByAttribute(ReactomeJavaConstants.CatalystActivity,
                ReactomeJavaConstants.physicalEntity,
                "=",
                entities);
        if (collection != null && collection.size() > 0) {
            Collection collection1 = dba.fetchInstanceByAttribute(ReactomeJavaConstants.ReactionlikeEvent,
                    ReactomeJavaConstants.catalystActivity,
                    "=",
                    collection);
            if (collection1 != null)
                reactions.addAll(collection1);
        }
        // Regulation based on RegulatedEntity
        collection = dba.fetchInstanceByAttribute(ReactomeJavaConstants.Regulation,
                ReactomeJavaConstants.regulator,
                "=",
                entities);
        if (collection != null && collection.size() > 0) {
            for (Iterator it = collection.iterator(); it.hasNext(); ) {
                GKInstance tmp = (GKInstance) it.next();
                if (tmp.getSchemClass().isa(ReactomeJavaConstants.ReactionlikeEvent))
                    reactions.add(tmp);
            }
        }
        return reactions;
    }

    List<List<GKInstance>> getAncestorPaths(GKInstance pathway) throws Exception {
        List<List<GKInstance>> paths = new ArrayList<List<GKInstance>>();
        List<GKInstance> firstPath = new ArrayList<GKInstance>();
        paths.add(firstPath);
        getAncestorPaths(pathway, firstPath, paths);
        mergePaths(paths);
        return paths;
    }

    private void getAncestorPaths(GKInstance pathway,
                                  List<GKInstance> firstPath,
                                  List<List<GKInstance>> paths) throws Exception {
        firstPath.add(0, pathway);
        List<GKInstance> parents = new ArrayList<GKInstance>();
        Collection collection = pathway.getReferers(org.gk.model.ReactomeJavaConstants.hasEvent);
        if (collection != null)
            parents.addAll(collection);
        collection = pathway.getReferers(ReactomeJavaConstants.hasMember);
        if (collection != null)
            parents.addAll(collection);
        collection = pathway.getReferers(ReactomeJavaConstants.hasSpecialisedForm);
        if (collection != null)
            parents.addAll(collection);
        if (parents.size() == 0)
            return;
        int c = 0;
        // firstPath might be changes. Back it up
        List<GKInstance> firstPathBackup = null;
        if (parents.size() > 1)
            firstPathBackup = new ArrayList<GKInstance>(firstPath);
        for (GKInstance parent : parents) {
            if (c == 0)
                getAncestorPaths(parent, firstPath, paths);
            else {
                // Copy the original Paths
                List<GKInstance> morePath = new ArrayList<GKInstance>(firstPathBackup);
                paths.add(morePath);
                getAncestorPaths(parent, morePath, paths);
            }
            c++;
        }
    }

    private void mergePaths(List<List<GKInstance>> paths) {
        for (int i = 0; i < paths.size(); i++) {
            List<GKInstance> path = paths.get(i);
            if (path == null)
                continue;
            for (int j = i + 1; j < paths.size(); j++) {
                List<GKInstance> path1 = paths.get(j);
                if (path1 == null)
                    continue;
                if (mergePaths(path, path1)) // Merge out
                    paths.set(j, null);
            }
        }
        // Trim the paths
        for (Iterator it = paths.iterator(); it.hasNext(); ) {
            Object path = it.next();
            if (path == null)
                it.remove();
        }
    }

    private boolean mergePaths(List<GKInstance> path1, List<GKInstance> path2) {
        // Check if these two paths belong to the same branch
        GKInstance top1 = path1.get(0);
        GKInstance top2 = path2.get(0);
        if (top1 != top2)
            return false;
        int size1 = path1.size();
        int size2 = path2.size();
        // Trim the first path1 if it is too long
        for (int i = size2; i < size1; i++)
            path1.remove(size2);
        // Compare the content
        size1 = path1.size();
        int cutoff = -1;
        for (int i = 0; i < size1; i++) {
            GKInstance instance1 = path1.get(i);
            GKInstance instance2 = path2.get(i);
            if (instance1 != instance2) {
                cutoff = i;
                break;
            }
        }
        if (cutoff > 0) {
            for (int i = cutoff; i < size1; i++)
                path1.remove(cutoff);
        }
        return true;
    }

    /**
     * A helper method to list all participants in a Complex.
     *
     * @param complex
     * @param participants
     */
    private void listComplexParticipantsI(Complex complex,
                                          Set<Complex> touchedComplexes,
                                          Set<PhysicalEntity> participants) {
        if (touchedComplexes.contains(complex))
            return;
        Set<Complex> current = new HashSet<Complex>();
        Set<Complex> next = new HashSet<Complex>();
        current.add(complex);
        while (current.size() > 0) {
            for (Complex complexTmp : current) {
                List<PhysicalEntity> list = complexTmp.getHasComponent();
                if (list == null || list.size() == 0)
                    continue;
                for (PhysicalEntity entity : list) {
                    if (entity instanceof Complex)
                        next.add((Complex) entity);
                    else
                        participants.add(entity);
                }
            }
            current.clear();
            current.addAll(next);
            next.clear();
        }
        touchedComplexes.add(complex);
    }
}