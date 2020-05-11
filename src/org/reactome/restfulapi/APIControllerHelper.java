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
import org.gk.sbgn.Dumper;
import org.gk.sbgn.SBGNBuilderFields;
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
import java.sql.*;
import java.util.*;
import java.util.List;

@Singleton
@SuppressWarnings({"unchecked", "rawtypes", "WeakerAccess"})
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
      This method is synchronized to avoid a potential thread conflicts caused by not-that-sophisticated
      caching mechanisms implemented in MySQLAdaptor, causing modified element exception in the Collection framework.
        @param long dbID The ID of the pathway diagram requested
     */
    public synchronized org.w3c.dom.Document bioPaxExporter(String level,
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
    
	/**
	 * Export a Pathway into SBGN.
	 * Only a Pathway can be exported, otherwise an empty string will be returned.
	 * @param dbId
	 * @return
	 */
    public String sbgnExport(long dbId) {
		try {
			GKInstance inst = dba.fetchInstance(dbId);
			if (!inst.getSchemClass().isa(ReactomeJavaConstants.Pathway)) {
				logger.error(inst + " is not a Pathway in sbgnExport()!");
				return null;
			}
			SBGNBuilderFields sbgnBuilder = new SBGNBuilderFields();
			sbgnBuilder.getDatabaseConnectionHandler().setDatabaseAdaptor(dba);
			sbgnBuilder.addField("pid", Arrays.asList(String.valueOf(dbId)));
			sbgnBuilder.convertPathways();
			String prolog = "<?xml version='1.0' encoding='UTF-8'?>\n";
			String sbgnString = Dumper.dumpToString(sbgnBuilder.getPDExtractor().getSbgn());
			return prolog + sbgnString;
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
    
    public List<PhysicalToReferenceEntityMap> getPathwayParticipantPEToRefEntityMap(String pathwayId){
    	Set<GKInstance> pes = new HashSet<GKInstance>(); 
    	try{
            GKInstance pathway = getInstance(pathwayId);
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
     * Connect to the denormalized database
     * @throws SQLException 
     * 
     */
    private Connection getDNConnection() throws SQLException {
    	String connectionStr = "jdbc:mysql://" + dba.getDBHost() + ":" + dba.getDBPort() + "/" + dba.getDBName() + "_dn&useUnicode=true&characterEncoding=utf-8";
    	Properties prop = new Properties();
    	prop.setProperty("user", dba.getDBUser());
    	prop.setProperty("password", dba.getDBPwd());
    	Connection conn = DriverManager.getConnection(connectionStr, prop);
    	return conn;
    }  

    // DEV-870 work starts here
    // http://stackoverflow.com/questions/624581/what-is-the-best-java-email-address-validation-method 
    private static boolean isValidEmailAddress(String email) {
    	String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
    	java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
    	java.util.regex.Matcher m = p.matcher(email);
    	return m.matches();
    }	
    
    public List<DatabaseObject> getPeopleByName(String name) {
    	try {
    		// split into first and last names
    		String[] names = name.split(" ");
    		
    		// ...but deal with situations where we only see one name
    		StringBuilder string = new StringBuilder();
    		if (names[0] != null && names.length > 1) {
    			string.append(names[0]);
    		}
    		String firstName = string.toString();
    		string.setLength(0);
    		
    		if (names.length > 1) {
    			string.append(names[names.length-1]);
    		}
    		String lastName = string.toString();
    		string.setLength(0);
    		
    		
    		// Watch out for apostrophes in names -- use double quotes
    		// and let's make matching a little fuzzy
    		if (firstName.length() > 0 && lastName.length() > 0) {
    			string.append("SELECT DB_ID from Person WHERE surname LIKE \"%"+ lastName + "%\""+
    				" AND firstname LIKE \"%"+ firstName + "%\"");
    		}
    		else {	
    			string.append("SELECT DB_ID from Person WHERE surname LIKE \"%"+ name + "%\""+
    				" OR firstname LIKE \"%"+ name + "%\"");
    		}
    		String sql = string.toString();
    		    		
    		Connection dbaConn = dba.getConnection();
    		Statement dbaStat = dbaConn.createStatement();
    		ResultSet resultSet = dbaStat.executeQuery(sql);
    		
    		List<DatabaseObject> rtn = new ArrayList<DatabaseObject>();
    		while (resultSet.next()) { 
    			long personId = resultSet.getLong(1);
                DatabaseObject person = fetchInstance("Person", String.valueOf(personId));
                rtn.add(person);
    		}
    	
    		return rtn;
    	}
    	catch(Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    	
    	return new ArrayList<DatabaseObject>();
    }
    
    public List<DatabaseObject> getPeopleByEmail(String email) {
    	try {
    		if (! isValidEmailAddress(email)) {
    			throw new IllegalArgumentException(email + " does not appear to be a valid email address.");
    		}

    		String sql = "SELECT DB_ID from Person WHERE eMailAddress LIKE '" + email + "'";

    		Connection dbaConn = dba.getConnection();
    		Statement dbaStat = dbaConn.createStatement();
    		ResultSet resultSet = dbaStat.executeQuery(sql);

    		List<DatabaseObject> rtn = new ArrayList<DatabaseObject>();
    		while (resultSet.next()) { 
    			Long personId = resultSet.getLong(1);
    			DatabaseObject person = fetchInstance("Person", String.valueOf(personId));
    			rtn.add(person);
    		}

    		return rtn;

    	}
    	catch(Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    
    	return new ArrayList<DatabaseObject>();
    }
    
    // DEV-870 work ends here
    
    // DEV-846 work starts here
    /**
     * Get a list of pathways that a person has authored
     * 
     * @param personId database identifier for an instance of the Person class
     * @return ArrayList<Pathway>
     */
    public List<Pathway> queryAuthoredPathways(long personId) {
    	try{
    		String sql = 
    				"SELECT d.DB_ID" +
    				" FROM  DatabaseObject AS d," +
    				" Event_2_authored AS e2a," +
    				" InstanceEdit_2_author AS i2a " + 
    				" WHERE i2a.author=" + String.valueOf(personId) + 
    				" AND   e2a.authored=i2a.DB_ID" + 
    				" AND   e2a.DB_ID=d.DB_ID" + 
    				" AND   d._class='Pathway'" ;
    		

    		Connection dbaConn = dba.getConnection();
    		Statement dbaStat = dbaConn.createStatement();
    		ResultSet resultSet = dbaStat.executeQuery(sql);

    		List<Pathway> rtn = new ArrayList<Pathway>();
    		while (resultSet.next()) { 
    			Long pathwayId = resultSet.getLong(1);
                GKInstance instance = dba.fetchInstance(pathwayId);
                Pathway pathway = (Pathway) converter.createObject(instance);
                rtn.add(pathway);
    		}

    		return rtn;
    	}
    	catch(Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    
        return new ArrayList<Pathway>();
    }
    /**
     * Get a list of pathways that a person has reviewed 
     * 
     * @param personId database identifier for an instance of the Person class
     * @return ArrayList<Pathway>
     */
    public List<Pathway> queryReviewedPathways(long personId) {
    	try{
    		String sql = 
    				"SELECT d.DB_ID" +
    				" FROM  DatabaseObject AS d," +
    				" Event_2_reviewed AS e2r," +
    				" InstanceEdit_2_author AS i2a " + 
    				" WHERE i2a.author=" + String.valueOf(personId) + 
    				" AND   e2r.reviewed=i2a.DB_ID" + 
    				" AND   e2r.DB_ID=d.DB_ID" + 
    				" AND   d._class='Pathway'" ;
    		
    		//System.err.println("MY SQL: " + sql);
    		

    		Connection dbaConn = dba.getConnection();
    		Statement dbaStat = dbaConn.createStatement();
    		ResultSet resultSet = dbaStat.executeQuery(sql);

    		List<Pathway> rtn = new ArrayList<Pathway>();
    		while (resultSet.next()) { 
    			Long pathwayId = resultSet.getLong(1);
                GKInstance instance = dba.fetchInstance(pathwayId);
                Pathway pathway = (Pathway) converter.createObject(instance);
                rtn.add(pathway);
    		}

    		return rtn;
    	}
    	catch(Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    
        return new ArrayList<Pathway>();
    }
    // DEV-846 work ends here

    
    /**
     * Query a list of pathways containing one or more genes from the passed gene
     * array.
     * @param genes gene symbols
     * @return
     */
    public List<Pathway> queryHitPathways(String[] genes) {
        try {
        	Connection dnConn = getDNConnection();
            String sql = "SELECT pr.pathwayId " + 
            		"FROM PhysicalEntity p, Id_To_ExternalIdentifier e, "+
            		"Pathway_To_ReactionLikeEvent pr, "+
            		"ReactionLikeEvent_To_PhysicalEntity r " + 
            		"WHERE e.id = p.id and p.id = r.physicalEntityId " + 
            		"AND r.reactionLikeEventId = pr.reactionLikeEventId "+
            		"AND p.species = 'Homo sapiens'" +
            		"AND e.externalIdentifier IN(";
            StringBuilder builder = new StringBuilder();
            for (String gene : genes) {
                builder.append("'").append(gene).append("',");
            }
            builder.deleteCharAt(builder.length() - 1); // Delete the last ","
            sql += builder.toString() + ")";
            System.err.println(sql);
            
            Statement stat = dnConn.createStatement();
            ResultSet resultSet = stat.executeQuery(sql);
            
            // Get a list of Pathway ids containing genes
            builder.setLength(0);
            Set<Long> pathwayIds = new HashSet<Long>();
            while (resultSet.next()) {
//            	builder.append(resultSet.getLong(1)).append(",");
            	pathwayIds.add(resultSet.getLong(1));
            }
//            if (builder.length() == 0) { // Nothing returned
//            	resultSet.close();
//            	stat.close();
//            	dnConn.close();
//            	return new ArrayList<Pathway>();
//            }
//            builder.deleteCharAt(builder.length() - 1);
//            resultSet.close();
//
//            sql = "SELECT pathwayId  \n" + 
//            		"FROM Pathway_To_ReactionLikeEvent pr \n" + 
//            		"WHERE pr.reactionLikeEventId IN(" + builder.toString() + ")";
//            System.err.println(sql);
//            resultSet = stat.executeQuery(sql);
//
//            // Get a list of Pathway ids containing genes
//            builder.setLength(0);
//            Set<Long> pathwayIds = new HashSet<Long>();
//            while (resultSet.next()) {
//            	pathwayIds.add(resultSet.getLong(1));
//            }
            if (pathwayIds.size() == 0) { // Nothing returned
            	resultSet.close();
            	stat.close();
            	dnConn.close();
            	return new ArrayList<Pathway>();
            }
          
            DiagramGeneratorFromDB diagramHelper = new DiagramGeneratorFromDB();
            diagramHelper.setMySQLAdaptor(dba);
            
            List<Pathway> rtn = new ArrayList<Pathway>();
            for (Long dbId : pathwayIds) {
            	GKInstance instance = dba.fetchInstance(dbId);
//            	GKInstance diagram = diagramHelper.getPathwayDiagram(instance);
//            	if (diagram != null) {
            		Pathway pathway = (Pathway) converter.createObject(instance);
            		rtn.add(pathway);
//            	}            	
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
            DiagramGeneratorFromDB diagramHelper = new DiagramGeneratorFromDB();
            diagramHelper.setMySQLAdaptor(dba);
            // Find PathwayDiagram
            GKInstance diagram = diagramHelper.getPathwayDiagram(pathway);
            if (diagram == null) {
                //logger.error("Pathway diagram is not available for " + pathway.getDisplayName());
                throw new IllegalStateException("Pathway diagram is not available for " + pathway.getDisplayName());
            }
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
            PathwayEditor editor = diagramHelper.preparePathwayEditor(diagram, 
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
                rtn = org.apache.commons.codec.binary.Base64.encodeBase64String(baos.toByteArray());
                baos.close();
            } 
            else if (type.equals("pdf")) {
                String uuid = UUID.randomUUID().toString();
                // To avoid a long name that is not supported by a platform
                File pdfFileName = new File(outputdir, uuid + ".pdf");
                BufferedImage image = SwingImageCreator.createImage(editor);
                ImageIO.write(image, "png", pdfFileName);
                SwingImageCreator.exportImageInPDF(editor, pdfFileName);
                byte[] pdfBytes = org.apache.commons.io.FileUtils.readFileToByteArray(pdfFileName);
                rtn = org.apache.commons.codec.binary.Base64.encodeBase64String(pdfBytes);
                boolean result = pdfFileName.delete();
                if(result==false)
                {
                    throw new Exception("Pathway diagram file could not be deleted.");
                }
            } 
            else {
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
    
    public DatabaseObject queryById(String className, 
                                    String id) {
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
    private DatabaseObject fetchInstance(String className, String id) throws Exception {
        GKInstance instance = getInstance(id);
        if (instance == null) {
        	throw new InstanceNotFoundException(className, id);
        }
        return converter.convert(instance);
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

    public Map<String, DatabaseObject> mapByIds(String className,
                                                List<String> ids){
        Map<String, DatabaseObject> rtn  = new HashMap<String, DatabaseObject>();
        try {
            for (String id : ids) {
                DatabaseObject converted = fetchInstance(className, id);
                if (converted != null)
                    rtn.put(id, converted);
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

    private GKInstance getInstance(String identifier) throws Exception {
        identifier = identifier.trim().split("\\.")[0];
        if (identifier.startsWith("REACT")){
            return getInstance(dba.fetchInstanceByAttribute(ReactomeJavaConstants.StableIdentifier, "oldIdentifier", "=", identifier));
        }else if (identifier.startsWith("R-")) {
            return getInstance(dba.fetchInstanceByAttribute(ReactomeJavaConstants.StableIdentifier, ReactomeJavaConstants.identifier, "=", identifier));
        } else {
            return dba.fetchInstance(Long.parseLong(identifier));
        }
    }

    private GKInstance getInstance(Collection<GKInstance> target) throws Exception {
        if(target==null || target.size()!=1) throw new Exception("Multiple options have been found for the specified identifier");
        GKInstance stId = target.iterator().next();
        return (GKInstance) dba.fetchInstanceByAttribute(ReactomeJavaConstants.DatabaseObject, ReactomeJavaConstants.stableIdentifier, "=", stId).iterator().next();
    }

    /**
     * There are two possibilities here. 1) The specified object has a direct link to the
     * demanded orthologous or 2) we need to go through the main species to try to find it.
     * @param dbId the identifier of the initial object
     * @param speciesId the identifier of the species the orthologous is required
     * @return the orthologous object for the specified species or null if not found
     */
    public DatabaseObject getOrthologous(String dbId, String speciesId){
        try {
            GKInstance species = getInstance(speciesId);
            GKInstance instance = getInstance(dbId);

            //Some initial checking before looking for something which does not make sense :)
            if(!instance.getSchemClass().isValidAttribute(ReactomeJavaConstants.species)){
                return null;
            }
            List speciess = instance.getAttributeValuesList(ReactomeJavaConstants.species);
            if(speciess==null || speciess.isEmpty()) return null;
            GKInstance target = (GKInstance) speciess.get(0);
            if(target.getDBID().equals(species.getDBID())) return converter.convert(instance);
            //Checking finished. It makes sense to continue looking for it after this point

            List orths = new ArrayList();
            if (instance.getSchemClass().isValidAttribute(ReactomeJavaConstants.orthologousEvent)) {
                List aux = instance.getAttributeValuesList(ReactomeJavaConstants.orthologousEvent);
                if (aux != null) orths.addAll(aux);
            }
            if (instance.getSchemClass().isValidAttribute(ReactomeJavaConstants.inferredFrom)) {
                List aux = instance.getAttributeValuesList(ReactomeJavaConstants.inferredFrom);
                if (aux != null) orths.addAll(aux);
            }

            for (Object item : orths) {
                GKInstance orth = (GKInstance) item;
                if(orth.getSchemClass().isValidAttribute(ReactomeJavaConstants.species)){
                    speciess = orth.getAttributeValuesList(ReactomeJavaConstants.species);
                    if(speciess!=null){
                        for (Object s : speciess) {
                            target = (GKInstance) s;
                            if(target.getDBID().equals(species.getDBID())){
                                return converter.convert(orth);
                            }else if(target.getDBID().equals(48887L)){
                                return getOrthologous(orth.getDBID().toString(), speciesId);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
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
            rtn = getPathwaysFromReactions(reactions);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return rtn;
    }

    public List<Pathway> queryPathwaysWithDiagramForEntity(Long dbId) {
        //Convert entity to GKInstances so that database query can be done for performance reason
        List<Pathway> rtn = new ArrayList<Pathway>();
        try {
            Set<GKInstance> instances = new HashSet<GKInstance>();
            GKInstance target = dba.fetchInstance(dbId);
            instances.add(target);
            instances.addAll(queryHelper.getReferersFor(target, "hasComponent", "hasMember", "hasCandidate", "repeatedUnit"));
//            instances.addAll(grepComplexesForEntities(instances));
            Set<GKInstance> reactions = getParticipatingReactions(instances);
            for (Pathway pathway : getPathwaysFromReactions(reactions)) {
                if(pathway.getHasDiagram()){
                    rtn.add(pathway);
                }else{
                    List<Event> events = queryAncestors(pathway.getDbId()).get(0);
                    ListIterator<Event> li = events.listIterator(events.size());
                    while(li.hasPrevious()){
                        Event event = li.previous();
                        if(event instanceof Pathway){
                            Pathway p = (Pathway) event;
                            if(p.getHasDiagram()){
                                rtn.add(p);
                                break;
                            }
                        }
                    }
                }
            }
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


    private List<Pathway> getPathwaysFromReactions(Collection reactions) throws Exception {
    	// Create the paths from the passed reactions
    	List<List<GKInstance>> paths = new ArrayList<List<GKInstance>>();
    	for (Iterator it = reactions.iterator(); it.hasNext(); ) {
    		GKInstance reaction = (GKInstance) it.next();
    		List<List<GKInstance>> paths1 = getParentPathways(reaction);
    		paths.addAll(paths1);
    	}
    	mergePaths(paths);

    	// Only Pathways are needed
    	List<Pathway> rtn = new ArrayList<Pathway>();
    	for (List<GKInstance> path : paths) {
    		for (int i = path.size() - 1; i >= 0; i--) {
    			GKInstance bottom = path.get(i);		
    			if (bottom.getSchemClass().isa(ReactomeJavaConstants.Pathway)) {
    				Pathway pathway = (Pathway) converter.createObject(bottom);
    				rtn.add(pathway);
    			}
    		}
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

    List<List<GKInstance>> getParentPathways(GKInstance reaction) throws Exception {
        List<List<GKInstance>> paths = new ArrayList<List<GKInstance>>();
        List<GKInstance> firstPath = new ArrayList<GKInstance>();
        getParentPathways(reaction, firstPath, paths);
        
        if (paths.size() == 0) {
        	paths.add(firstPath);
        }
        mergePaths(paths);
        return paths;
    }

    private void getParentPathways(GKInstance pathway,
                                  List<GKInstance> firstPath,
                                  List<List<GKInstance>> paths) throws Exception {
        firstPath.add(0, pathway);
        List<GKInstance> parents = new ArrayList<GKInstance>();
        Collection collection = pathway.getReferers(org.gk.model.ReactomeJavaConstants.hasEvent);

        if (collection != null) {
            parents.addAll(collection);
        }
        
        collection = pathway.getReferers(ReactomeJavaConstants.hasMember);
        if (collection != null) {
            parents.addAll(collection);
        }
        
        collection = pathway.getReferers(ReactomeJavaConstants.hasSpecialisedForm);
        if (collection != null) {
            parents.addAll(collection);
        }
                
        if (parents.size() == 0)
            return;

        if (parents.size() > 0) {
        	paths.add(parents);
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

    public List<String> getReferenceMolecules() {
    	try {
    		Collection<GKInstance> instances = dba.fetchInstancesByClass(ReactomeJavaConstants.ReferenceMolecule);
    		dba.loadInstanceAttributeValues(instances, new String[] {ReactomeJavaConstants.identifier,
    				ReactomeJavaConstants.referenceDatabase});
    		List <String> rtn = new ArrayList<String>();
    		for (GKInstance inst : instances) {
    			GKInstance refDb = (GKInstance) inst.getAttributeValue(ReactomeJavaConstants.referenceDatabase);
    			String dbName = refDb.getDisplayName();
    			String id = (String) inst.getAttributeValue(ReactomeJavaConstants.identifier);
    			String DB_ID = inst.getDBID().toString();
    			rtn.add(DB_ID + "\t" + dbName + ":" + id); 
    		}
    		return rtn;
    	}
    	catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    	return new ArrayList<String>();
    }

	public List<String> getDiseases() {
    	try {
    		Collection<GKInstance> instances = dba.fetchInstancesByClass(ReactomeJavaConstants.Disease);
    		dba.loadInstanceAttributeValues(instances, new String[] {ReactomeJavaConstants.identifier,
    				ReactomeJavaConstants.referenceDatabase});
    		List <String> rtn = new ArrayList<String>();
    		for (GKInstance inst : instances) {
    			GKInstance refDb = (GKInstance) inst.getAttributeValue(ReactomeJavaConstants.referenceDatabase);
    			String dbName = refDb.getDisplayName();
    			String id = (String) inst.getAttributeValue(ReactomeJavaConstants.identifier);
    			String DB_ID = inst.getDBID().toString();
    			rtn.add(DB_ID + "\t" + dbName + ":" + id); 
    		}
    		return rtn;
    	}
    	catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
		return new ArrayList<String>();
	}

	public List<String> getUniProtRefSeqs() {
    	try {
    		String sql1 = "SELECT DB_ID from DatabaseObject WHERE _displayName='UniProt' AND _class='ReferenceDatabase'";
    		    		
    		Connection dbaConn = dba.getConnection();
    		Statement dbaStat = dbaConn.createStatement();
    		ResultSet resultSet = dbaStat.executeQuery(sql1);
    		resultSet.next(); 
    		String DB_ID = resultSet.getString(1);
  
    		String sql2 = "SELECT re.DB_ID, re.identifier FROM ReferenceEntity re, ReferenceSequence rs "+
    				"WHERE rs.DB_ID=re.DB_ID AND referenceDatabase=" + DB_ID;
    		
    		Statement dbaStat2 = dbaConn.createStatement();
    		ResultSet resultSet2 = dbaStat2.executeQuery(sql2);
    		
    		List<String> rtn = new ArrayList<String>();
    		while (resultSet2.next()) {
    			String id = resultSet2.getString(1);
    			String accession = resultSet2.getString(2);
    			rtn.add(id + "\tUniProt:" + accession);
    		}
    		
    		return rtn;
    	}
    	catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
		return new ArrayList<String>();
	}
	
	
}
