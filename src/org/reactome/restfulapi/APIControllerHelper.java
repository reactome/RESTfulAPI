package org.reactome.restfulapi;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
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
import org.gk.render.RenderablePathway;
import org.gk.sbml.SBMLAndLayoutBuilderFields;
import org.gk.schema.InvalidAttributeException;
import org.gk.schema.SchemaAttribute;
import org.gk.schema.SchemaClass;
import org.gk.util.SwingImageCreator;
import org.jdom.output.DOMOutputter;
import org.reactome.biopax.ReactomeToBioPAX3XMLConverter;
import org.reactome.biopax.ReactomeToBioPAXXMLConverter;
import org.reactome.restfulapi.details.pmolecules.ParticipatingMolecules;
import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;
import org.reactome.restfulapi.models.Complex;
import org.reactome.restfulapi.models.DatabaseObject;
import org.reactome.restfulapi.models.ListOfShellInstances;
import org.reactome.restfulapi.models.Pathway;
import org.reactome.restfulapi.models.PhysicalEntity;
import org.reactome.restfulapi.models.Publication;
import org.springframework.util.StringUtils;

import com.googlecode.gwt.crypto.gwtx.io.IOException;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.core.impl.provider.entity.Inflector;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
public class APIControllerHelper {
    private Logger logger = Logger.getLogger(APIControllerHelper.class);
    private MySQLAdaptor dba;
    private ReactomeToRESTfulAPIConverter converter;
    private QueryHelper queryHelper;
    private String outputdir;

    public String getOutputdir() {
        return outputdir;
    }

    public void setOutputdir(String outputdir) {
        this.outputdir = outputdir;
    }

    public APIControllerHelper() {
    }

    public void setConverter(ReactomeToRESTfulAPIConverter converter) {
        this.converter = converter;
        queryHelper.setMapper(converter.getMapper());
    }

    public void setDba(MySQLAdaptor dba) {
        this.dba = dba;
        queryHelper.setMySQLAdaptor(dba);
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
     * @param dbId
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
    

    public String pathwayDiagram(final long pathwayId, final String type) {
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
                PathwayDiagramXMLGenerator xmlGenerator = new PathwayDiagramXMLGenerator();
                return xmlGenerator.generateXMLForPathwayDiagram(diagram, pathway);
            }
            DiagramGKBReader reader = new DiagramGKBReader();
            RenderablePathway renderablePathway = reader.openDiagram(diagram);
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
        } catch (QueryNotSupportedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    boolean isStableIdentifier(final String Id)
    {
        String StrId = String.valueOf(Id);
        boolean result = StrId.matches("REACT.*");
        return result;
    }
    
    public DatabaseObject queryById(String className, final String dbId) {
        GKInstance instance;
        DatabaseObject rtn = new DatabaseObject();
        try {
            if(isStableIdentifier(dbId)==false)
            {
                Long dbIdl = Long.parseLong(dbId);
                instance = dba.fetchInstance(className, dbIdl);
                
                if (instance == null) {
                    throw new InstanceNotFoundException(dbIdl);
                }
                rtn = (DatabaseObject) converter.convert(instance);
            }
            else
            {
                Collection col = dba.fetchInstanceByAttribute(ReactomeJavaConstants.StableIdentifier, ReactomeJavaConstants.identifier, "=", dbId);
                Collection Objcol = dba.fetchInstanceByAttribute(className, ReactomeJavaConstants.stableIdentifier, "=", (GKInstance)col.iterator().next());
                if (Objcol == null || Objcol.size() == 0) {
                    throw new InstanceNotFoundException(Long.parseLong(dbId.substring(6)));
                }
                rtn = (DatabaseObject) converter.convert((GKInstance)Objcol.iterator().next());
                //System.out.print(className+":"+rtn.toString());
            }
        }
        catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    public String queryByIds(String className, final List<String> dbIds, final String accept) {
        GKInstance instance;
        String rtn = null;
        ObjectMapper mapper = new ObjectMapper();
        int headerType = getHeaderType(accept);
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        String pluralClass = Inflector.getInstance().pluralize(className);
        StringWriter sw = new StringWriter();
        StringBuilder clsName = new StringBuilder();
        clsName.append("org.reactome.restfulapi.models.");
        clsName.append(className);
        if(headerType==0)
            sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<").append(pluralClass.toLowerCase()).append(">\n");

        try {
            Class cls = Class.forName(clsName.toString());
            Object obj = cls.newInstance();
            JAXBContext context = new JSONJAXBContext(JSONConfiguration.mapped().rootUnwrapping(false).build(), obj.getClass());
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.setProperty(Marshaller.JAXB_FRAGMENT, true);
            DatabaseObject object = null;
            for (String id : dbIds) {
                if(isStableIdentifier(id)==false)
                {
                    Long dbIdl = Long.parseLong(id);
                    instance = dba.fetchInstance(className, dbIdl);

                    if (instance == null) {
                    throw new InstanceNotFoundException(dbIdl);
                    }
                    object = (DatabaseObject) converter.convert(instance);
                    if (object == null) {
                    throw new InstanceNotFoundException(dbIdl);
                    }
                }
                else {
                    Collection col = dba.fetchInstanceByAttribute(ReactomeJavaConstants.StableIdentifier, ReactomeJavaConstants.identifier, "=", id);
                    Collection Objcol = dba.fetchInstanceByAttribute(className, ReactomeJavaConstants.stableIdentifier, "=", (GKInstance)col.iterator().next());
                    if (Objcol == null || Objcol.size() == 0) {
                        throw new InstanceNotFoundException(Long.parseLong(id.substring(6)));
                    }
                    object = (DatabaseObject) converter.convert((GKInstance)Objcol.iterator().next());
                    if (object == null) {
                    throw new InstanceNotFoundException(Long.parseLong(id.substring(6)));
                    }

                }
                if(headerType==0)
                {
                    m.marshal(object, sw);
                    sw.append("\n");
                }
                else if(headerType==1)
                {
                    mapper.writeValue(sw, object);
                }
                else if(headerType==-1)
                {   //by default, it will be XML
                   m.marshal(object, sw);
                    sw.append("\n");
                }
                else
                {
                    throw new Exception("Unhandled headerType value");
                }
            } //end for
            if(headerType==0)
                sw.append("</").append(pluralClass.toLowerCase()).append(">");

            rtn = sw.toString();
            sw.close();
        } catch (InstanceNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtn;
    }

    /*
    @param String accept
    @return:
       -1 = OTHER
        0 = XML
        1 = JSON
        2 = TEXT
     */
    int getHeaderType(String accept)
    {
        boolean text = false;

        if(accept.length()!=0)
        {
            String[] acceptArray = StringUtils.trimAllWhitespace(accept).split(",");
            for(String acceptType: acceptArray)
            {
                if(acceptType.equalsIgnoreCase(MediaType.APPLICATION_XML))
                {
                    return 0;
                }
                else if(accept.equalsIgnoreCase(MediaType.APPLICATION_JSON))
                {
                    return 1;
                }
                else if(accept.equalsIgnoreCase(MediaType.TEXT_PLAIN))
                {
                    text = true;
                }
            }
            return (text == true) ? 2: -1;
        }
        else {
            return -1;
        }
    }

    public String listByQuery(String className, 
                              String propertyName, 
                              String propertyValue, 
                              String accept) {
        //currenty this only returns proxies
        List rtn = new ArrayList();
        List<GKInstance> instances;
        StringWriter sw = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        int headerType = getHeaderType(accept);
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        if(headerType==0)
                sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<").append(className).append(">\n");

        try {
            instances = queryHelper.query(className, propertyName, propertyValue);
            if(instances.size()==0)
                throw new InstanceNotFoundException(className,propertyValue);

            for (GKInstance gkInstance : instances) {
                DatabaseObject converted = (DatabaseObject) converter.convert(gkInstance);
                if(headerType==1)
                {
                    mapper.writeValue(sw, converted);
                }
                else
                {  //default and everything else to XML

                    JAXBContext context = JAXBContext.newInstance(converted.getClass());
                    Marshaller m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    m.setProperty(Marshaller.JAXB_FRAGMENT, true);
                    m.marshal(converted, sw);
                }

            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        if(headerType==0)
                sw.append("</").append(className).append(">");

        return sw.toString();
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
//        StringWriter sw = new StringWriter();
//        ObjectMapper mapper = new ObjectMapper();
//        int headerType = getHeaderType(accept);
//        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
//        mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
//        mapper.getSerializationConfig().setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
//
//        Set<Event> current = new HashSet<Event>();
//        try {
//            Event dbPathway = loadPathway(PathwayId);
//            // Get all reactions first from the specified Pathway
//            current.add(dbPathway);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        Set<Event> next = new HashSet<Event>();
//        Set<Reaction> reactions = new HashSet<Reaction>();
//        while (current.size() > 0) {
//            for (Event eventTmp : current) {
//                if (eventTmp instanceof Reaction) {
////                    System.out.print(eventTmp + "y");
//                    reactions.add((Reaction) eventTmp);
//                } else if (eventTmp instanceof Pathway) {
////                    System.out.print(eventTmp + "x");
//                    Pathway pathway1 = (Pathway) eventTmp;
//                    if (pathway1.getHasEvent() != null) {
////                        System.out.print(eventTmp + "z");
//                        next.addAll(pathway1.getHasEvent());
//                    }
//                }
//            }
//            current.clear();
//            current.addAll(next);
//            next.clear();
//        }
//        Set<PhysicalEntity> entitySet = new HashSet<PhysicalEntity>();
//        Set<Complex> touchedComplexes = new HashSet<Complex>();
//        for (Reaction reaction : reactions) {
//            if (reaction.getInput() != null) {
//                List<PhysicalEntity> inputs = reaction.getInput();
//                if (inputs != null) {
//                    for (PhysicalEntity entity : inputs) {
//                        if (entity instanceof Complex)
//                            listComplexParticipantsI((Complex) entity,
//                                    touchedComplexes,
//                                    entitySet);
//                        else
//                            entitySet.add(entity);
//                    }
//                }
//            }
//            if (reaction.getOutput() != null) {
//                List<PhysicalEntity> outputs = reaction.getOutput();
//                if (outputs != null) {
//                    for (PhysicalEntity entity : outputs) {
//                        if (entity instanceof Complex)
//                            listComplexParticipantsI((Complex) entity,
//                                    touchedComplexes,
//                                    entitySet);
//                        else
//                            entitySet.add(entity);
//                    }
//                }
//            }
//            if (reaction.getCatalystActivity() != null) {
//                List<CatalystActivity> cas = reaction.getCatalystActivity();
//                for (CatalystActivity ca : cas) {
//                    PhysicalEntity catalyst = ca.getPhysicalEntity();
//                    if (catalyst instanceof Complex)
//                        listComplexParticipantsI((Complex) catalyst,
//                                touchedComplexes,
//                                entitySet);
//                    else if (catalyst != null)
//                        entitySet.add(catalyst);
//                }
//            }
//            if (reaction.getRegulation() != null) {
//                List<Regulation> regulations = reaction.getRegulation();
//                for (Regulation regulation : regulations) {
//                    DatabaseObject regulator = regulation.getRegulator();
//                    if (regulator instanceof Complex)
//                        listComplexParticipantsI((Complex) regulator,
//                                touchedComplexes,
//                                entitySet);
//                    else if (regulator instanceof ReactionlikeEvent)
//                        entitySet.add((PhysicalEntity) regulator);
//                }
//            }
//        }
//
//        String physicalEntityName = Inflector.getInstance().pluralize(ReactomeJavaConstants.PhysicalEntity);
//        if(headerType==0 || headerType == -1) // If no header has been set, XML will be returned as default.
//            sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<").append(physicalEntityName).append(">\n");
//
//        List<PhysicalEntity> entityList = new ArrayList<PhysicalEntity>(entitySet);
//        try
//        {
//            for (int i = 0; i < entityList.size(); i++) {
//                if(headerType==0 || headerType==-1)
//                {
//                    JAXBContext context = JAXBContext.newInstance(entityList.get(i).getClass());
//                    Marshaller m = context.createMarshaller();
//                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//                    m.setProperty(Marshaller.JAXB_FRAGMENT, true);
//                    m.marshal(entityList.get(i), sw);
//                }
//                else if(headerType==1)
//                {
//                    mapper.writeValue(sw, entityList.get(i));
//                }
//                else
//                {
//                    throw new Exception("Error marshalling object, unhandled headerType");
//                }
//                sw.append("\n");
//            }
//            if(!accept.equalsIgnoreCase(MediaType.APPLICATION_JSON))
//                sw.append("</").append(physicalEntityName).append(">");
//
//            sw.close();
//        }
//        catch(JAXBException e)
//        {
//            e.printStackTrace();
//        }
//        catch(Exception e)
//        {
//            e.printStackTrace();
//        }
//        // Sort it
//        /* Collections.sort(rtn, new Comparator() {
//            public int compare(Object obj1, Object obj2) {
//                PhysicalEntity entity1 = (PhysicalEntity) obj1;
//                PhysicalEntity entity2 = (PhysicalEntity) obj2;
//                return entity1.getName().compareTo(entity2.getName());
//            }
//        });*/
//        return sw.toString();
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
    
    public List<Pathway> listFrontPageItem() {
        // Get the FrontPage instance. It is assumed that there should be only one
        // FrontPage instance
        try {
            Collection c = dba.fetchInstancesByClass(ReactomeJavaConstants.FrontPage);
            if (c == null || c.size() == 0)
                return new ArrayList<Pathway>();
            GKInstance frontPage = (GKInstance) c.iterator().next();
            List<?> values = frontPage.getAttributeValuesList(ReactomeJavaConstants.frontPageItem);
            List<Pathway> pathways = new ArrayList<Pathway>(values.size());
            for (Iterator<?> it = values.iterator(); it.hasNext();) {
                GKInstance inst = (GKInstance) it.next();
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
            logger.error("Cannot queryLiteratureReferenceForPerson(): ", e);
        }
        return new ArrayList<Publication>();
    }
    
    public List<ListOfShellInstances> queryAncestors(Long eventId) {
        try {
            GKInstance event = dba.fetchInstance(eventId);
            return queryHelper.queryAncestors(event);
        }
        catch(Exception e) {
            logger.error("Cannot queryAncestors", e);
        }
        return new ArrayList<ListOfShellInstances>();
    }
    
    /**
     * Get the detailed view for a DatabaseObject specified by its DB_ID.
     * @param dbId
     * @return
     */
    public DatabaseObject getDetailedView(String className,
                                          String dbId) {
        DatabaseObject rtn = queryById(className, dbId);
        if (rtn != null && rtn.getDbId() > 0) {
            try {
                GKInstance instance = dba.fetchInstance(rtn.getDbId());
                converter.fillInDetails(instance, rtn);
            }
            catch(Exception e) {
                logger.error("getDetailedView()", e);
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
            e.printStackTrace();
        }
        if (instances.size() == 0)
            return new ArrayList<Pathway>();
//      Query the list of Reactions containing instances
        // Have to consider all complexes those GKInstances participate too
        try {
            Set<GKInstance> complexes = grepComplexesForEntities(instances);
            instances.addAll(complexes);
            Set<GKInstance> reactions = getParticipatingReactions(instances);
            rtn = grepTopPathwaysFromReactions(reactions);
        } catch (Exception e) {
            e.printStackTrace();
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
        List<GKInstance> topPathways = new ArrayList<GKInstance>();
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
        for (GKInstance instance : set) {
            Pathway pathway = (Pathway) converter.convert(instance);
            topPathways.add(instance);
        }
        // Convert the topPathways to Pathway objects
        List<Pathway> rtn = new ArrayList<Pathway>();
        for (GKInstance pathwayInstance : topPathways) {
            Pathway pathway = (Pathway) converter.convert(pathwayInstance);
            rtn.add(pathway);
        }
        return rtn;
    }

    private Set<GKInstance> getParticipatingReactions(Collection<GKInstance> entities) throws Exception {
        Set<GKInstance> reactions = new HashSet<GKInstance>();
        // Inputs
        Collection collection = dba.fetchInstanceByAttribute(ReactomeJavaConstants.Reaction,
                ReactomeJavaConstants.input,
                "=",
                entities);
        if (collection != null)
            reactions.addAll(collection);
        // Outputs
        collection = dba.fetchInstanceByAttribute(ReactomeJavaConstants.Reaction,
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
            Collection collection1 = dba.fetchInstanceByAttribute(ReactomeJavaConstants.Reaction,
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
                if (tmp.getSchemClass().isa(ReactomeJavaConstants.Reaction))
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

    public ResultContainer getParticipatingMolecules(Long paramID){
    	ResultContainer rtn;
    	ParticipatingMolecules pm = new ParticipatingMolecules();
    	pm.setDBA(dba);
    	try {
			rtn = pm.getParticipatingMolecules(paramID);
		} catch (IOException e) {
			rtn = new ResultContainer();
			rtn.setErrorMessage(e.getMessage());
			e.printStackTrace();
		}
    	return rtn;
    }
}