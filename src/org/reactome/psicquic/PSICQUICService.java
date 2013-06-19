/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gk.model.GKInstance;
import org.gk.model.InstanceUtilities;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.hupo.psi.mi.psicquic.registry.ServiceType;
import org.hupo.psi.mi.psicquic.registry.client.PsicquicRegistryClientException;
import org.hupo.psi.mi.psicquic.registry.client.registry.DefaultPsicquicRegistryClient;
import org.hupo.psi.mi.psicquic.registry.client.registry.PsicquicRegistryClient;
import org.junit.Test;
import org.reactome.psicquic.model.QueryResults;
import org.reactome.psicquic.model.SimpleQueryResult;
import org.reactome.psicquic.service.Service;

/**
 * 
 * JavaScript clients can't connect directly to PSICQUIC server so this servlet
 * is used as a proxy. At init time it queries the PSICQUIC registry to retrieve
 * a list of active REST services and returns them to the ELV (specifically the
 * ControlPanel).
 * User added PSICQUIC services are stored in a cookie and a session map. During
 * the lifetime of a session the url of the service is retrieved from the
 * session map. The cookie stored user added PSICQUIC services across sessions.
 * At run time when the user clicks 'Display Interactors' in the ELV a request
 * is submitted to this servlet which forwards the request onto the selected
 * PSICQUIC service.
 * If the user selects user-uploaded data to query instead of a PSICQUIC service
 * a local database is queried.
 * 
 * Query results are sent back in JSON format to ELV.
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
public class PSICQUICService {
    private static final Logger logger = Logger.getLogger(PSICQUICService.class);
//	private String psicquicRegistry; // The URL of the psicquic registry to query.
	
//	private int startIndex; // Equal to number of services returned from psicquic
//							// registry + 1. Used in each user session as starting
//							// point for adding user psicquic services.
//	
//	private Map<Integer, Service> indexServiceMap; // Maps an index to service urls. Client will
//								 // submit index so servlet can retrieve service url.
//	
//	private List<Map<String,String>> indexNameList; // Maps an index to service names. This is JSONified 
//							    // and sent back to the client so list of services can be filled
//								// in ControlPanel.
//	
	private int maxResults; // cap the number of results that will be returned to client
	private MySQLAdaptor dba;
	private Map<String, Service> nameToServiceMap;
	// For local temp file
	private String tempDir;
	
	public PSICQUICService() {
	    try {
	        retrievePsicquicServices();
	    }
	    catch(PsicquicRegistryClientException e) {
	        logger.error(e.getMessage(), e);
	    }
	}
	
	public void setTempDir(String dir) {
	    this.tempDir = dir;
	}
	
	public String getTempDir() {
	    return this.tempDir;
	}
	
	public void setMaxResults(int results) {
	    this.maxResults = results;
	}
	
	public int getMaxResults() {
	    return this.maxResults;
	}
	
	public void setMySQLAdaptor(MySQLAdaptor dba) {
	    this.dba = dba;
	}
	
	public MySQLAdaptor getMySQLAdaptor() {
	    return this.dba;
	}
	
//	/**
//	 * This method is used to do actual query.
//	 * @param action
//	 * @throws IOException
//	 */
//	public QueryResults processRequest(String action,
//	                           Integer serviceIndex,
//	                           Long pathwayId) throws IOException{
//		ActionType actionType = ActionType.getActionType(action);
//		
//		QueryResults res = null;
//		switch (actionType) {
//		case ALL_INTERACTIONS:
//			// Retrieve interactions for all proteins in pathway
//			res = processPIRequest(pathwayId, 
//			                       serviceIndex);
//			break;
//		case EXPORT:
//			// User has requested export of raw PSIMI-TAB data of all protein
//			// interactions
//			processEXRequest(mainReactomeDatabaseTools, request, response);
//			break;
//		case EXPORT_SUBSET:
//			// User has requested export of raw PSIMI-TAB data of a set of
//			// protein interactions
//			processEXIRequest(mainReactomeDatabaseTools, request, response);
//			break;
//		case NEW_SERVICE:
//			// User has requested add a new PSICQUIC service
//			processNEWPSRequest(request, response);
//			break;
//		case LIST_SERVICES:
//			// Query the PSICQUIC registry
//			processREGRequest(request, response);
//			break;
//		case DEFAULT:
//			res = processDefaultRequest(dnDatabaseTools, mainReactomeDatabaseTools, request);
//		}
//		
//		return res;
//	}
	
//	
//	private QueryResults processDefaultRequest(DatabaseTools dnDatabaseTools, ReactomeDatabaseTools mainReactomeDatabaseTools, HttpServletRequest request){
//		QueryResults res = new QueryResults();
//		String className = this.getClass().getName();
//		
//		//Getting some necessary values from the user request
//		//TODO: Check every variable for possible format errors
//		String retrievalmethod = request.getParameter("retrievalmethod");
//		if (retrievalmethod == null) {
//			System.err.println(className + ".processRequest: WARNING - retrievalmethod == null");
//			res.setErrorMessage("retrievalmethod == null");
//			return res;
//		}
//		int m = (-1);
//		try {
//			m = Integer.parseInt(retrievalmethod);
//		} catch (NumberFormatException e1) {
//			System.err.println(className + ".processRequest: WARNING - retrievalmethod = " + retrievalmethod + " is not an integer!");
//			res.setErrorMessage("retrievalmethod = " + retrievalmethod + " is not an integer!");
//			e1.printStackTrace(System.err);
//			return res;
//		}
//		MethodType method = MethodType.getMethodType(m);		
//		String refids = request.getParameter("refid");
//		int serviceIndex;
//		try{
//			serviceIndex = Integer.valueOf(request.getParameter("si"));
//		}
//		catch(Exception e){
//			serviceIndex = -1;
//		}
//		String label = request.getParameter("label");
//		
//		//TODO: Check this check because something tells me that is not good
//		if(refids == null || refids.length() == 0 || refids.matches("[^0-9]")){
//			System.err.println(className + ".processRequest: WARNING - Didn't find a valid refid.");
//			if (refids == null)
//				System.err.println(className + ".processRequest: refids == null");
//			else
//				System.err.println(className + ".processRequest: refids=" + refids);
//			System.err.println(className + ".processRequest: serviceIndex=" + serviceIndex);
//			System.err.println(className + ".processRequest: method=" + method.name());
//			if (label != null)
//				System.err.println(className + ".processRequest: label=" + label);
//			
//			res.setErrorMessage("Could not find a valid refid of the selected protein");
//			return res;
//		}
//		
//		Map<String, String> queryRefid = getAccToRefIdMapping(mainReactomeDatabaseTools, refids);
//		PSICQUICRetriever pr;
//		switch(method){
//		case USER:
//			// Local database (user-submitted data)
//			Map nameTableMap = (Map) this.session.getAttribute("nameTableMap");
//			String tableName = (String)nameTableMap.get(label);
//			UserDataRetriever udr = new UserDataRetriever(dnDatabaseTools, tableName);
//			
//			pr = new PSICQUICRetriever(udr.getUserDataResults());
//			res = pr.getDataFromRest(queryRefid, this.maxResults);
//			break;
//		case SOAP:
//			//Currently not supported because it was not being used
//			break;
//		case REST:
//		default:
//			pr = getPISCQUICRetrieverFromIndex(serviceIndex);
//			res = pr.getDataFromRest(queryRefid, this.maxResults);
//			break;			
//		}
//		
//		return res;
//	}
//	
	/**
	 * 
	 * Retrieve interactions for a pathway or PE specified by its DB_ID.
	 * @return
	 */
	public QueryResults queryInteractions(Long dbId,
	                                      String serviceName) {
	    try {
	        GKInstance dbObj = dba.fetchInstance(dbId);
	        // Currently it support PhysicalEntity only
	        if (dbObj.getSchemClass().isa(ReactomeJavaConstants.PhysicalEntity) ||
	            dbObj.getSchemClass().isa(ReactomeJavaConstants.Event)) {
	            Set<GKInstance> refSeqs = queryReferenceEntities(dbObj);
	            Map<String, String> accessionToRefSeqId = getAccToRefIdMapping(refSeqs);
	            PSICQUICRetriever pr = getPISCQUICRetrieverFromName(serviceName);
	            return pr.getDataFromRest(accessionToRefSeqId);
	        }
	        return new QueryResults(); // Create an empty array to avoid null exception
	    }
	    catch(Exception e) { // If there is an exception, send the error message to the client.
	        logger.error(e.getMessage(), e);
	        QueryResults results = new QueryResults();
	        results.setErrorMessage(e.getMessage());
	        return results;
	    }
	}
	
	/**
	 * Export protein-protein interactions as a single String.
	 * @param dbId
	 * @param serviceName
	 * @return
	 */
	public String exportInteractions(Long dbId,
	                                 String serviceName) {
	    try {
	        GKInstance dbObj = dba.fetchInstance(dbId);
            // Currently it support PhysicalEntity only
            if (dbObj.getSchemClass().isa(ReactomeJavaConstants.PhysicalEntity) ||
                dbObj.getSchemClass().isa(ReactomeJavaConstants.Event)) {
                Set<GKInstance> refSeqs = queryReferenceEntities(dbObj);
                Map<String, String> accessionToRefSeqId = getAccToRefIdMapping(refSeqs);
                PSICQUICRetriever pr = getPISCQUICRetrieverFromName(serviceName);
                return pr.exportInteractions(accessionToRefSeqId);
            }
	    }
	    catch(Exception e) {
	        logger.error(e.getMessage(), e);
	    }
	    return null;
	}
	
	/**
	 * Get a set of ReferenceEntities contained by an Event (Pathway or ReactionLikeEvent) or
	 * a PhysicalEntity.
	 * @param inst
	 * @return
	 * @throws Exception
	 */
	private Set<GKInstance> queryReferenceEntities(GKInstance inst) throws Exception {
	    if (inst.getSchemClass().isa(ReactomeJavaConstants.PhysicalEntity))
	        return InstanceUtilities.grepReferenceEntitiesForPE(inst);
	    if (inst.getSchemClass().isa(ReactomeJavaConstants.Event)) {
	        Set<GKInstance> pes = InstanceUtilities.grepPathwayParticipants(inst);
	        Set<GKInstance> set = new HashSet<GKInstance>();
	        for (GKInstance pe : pes) {
	            // Support EWASes only
	            if (pe.getSchemClass().isa(ReactomeJavaConstants.EntityWithAccessionedSequence))
	                set.addAll(InstanceUtilities.grepReferenceEntitiesForPE(pe));
	        }
	        return set;
	    }
	    return new HashSet<GKInstance>(); // Return an empty HashSet to avoid an null exception.
	}

	
	//	
//	/**
//	 * 
//	 * User has requested export of raw PSIMI-TAB data of all proteins interaction
//	 * 
//	 * @return
//	 */
//	private void processEXRequest(ReactomeDatabaseTools mainReactomeDatabaseTools, HttpServletRequest request,
//								  HttpServletResponse response){
//		String pathwayId = request.getParameter("pdbid");
//		String si = (String)request.getParameter("si");
//		int serviceIndex = si!=null?Integer.valueOf(si):-1;
//		
//		List accs = getAccessionsInPathway(mainReactomeDatabaseTools, pathwayId);
//		
//		try {
//			PrintWriter  out = response.getWriter();
//			
//			PSICQUICRetriever pr = getPISCQUICRetrieverFromIndex(serviceIndex);
//			pr.printRestRawData(accs, out);
//		} catch (IOException e) {
//			System.err.println(e);
//		}
//	}
//	
//	private void processEXIRequest(ReactomeDatabaseTools mainReactomeDatabaseTools, HttpServletRequest request,
//								   HttpServletResponse response){
//		//Getting some necessary values from the user request
//		//TODO: Check every variable for possible format errors 
//		//int m = Integer.parseInt(request.getParameter("retrievalmethod"));
//		String refids = request.getParameter("refid");
//		String si = request.getParameter("si");
//		int serviceIndex = si!=null?Integer.valueOf(si):-1;
//		
//		Map<String, String> queryRefid = getAccToRefIdMapping(mainReactomeDatabaseTools, refids);
//		
//		try {
//			PrintWriter  out = response.getWriter();
//			
//			PSICQUICRetriever pr = getPISCQUICRetrieverFromIndex(serviceIndex);
//			pr.printRestRawData(queryRefid, out);
//		} catch (IOException e) {
//			System.err.println(e);
//		}
//	}
//	
//	/**
//	 * 
//	 * @param request
//	 * @param response
//	 * @return
//	 */
//	private void processNEWPSRequest(HttpServletRequest request,
//											 HttpServletResponse response){
//		Cookie serviceCookie = null;
//		
//		// Get the current max index used for indexing user PSICQUIC services.
//		String currentIndexString = (String)session.getAttribute("index");
//		if(currentIndexString == null){
//			currentIndexString = "" + this.startIndex;
//		}
//		    
//	    String name = request.getParameter("name");
//		String serviceUrl = request.getParameter("url");
//		serviceUrl = serviceUrl.replaceAll("query/", "");
//		
//		//Setting session attributes -> index and indexUrlMap
//		synchronized(this.session){
//			String index = ""+(Integer.parseInt(currentIndexString)+1);
//			
//		    Map indexUrlMap = (Map)session.getAttribute("indexUrlMap");
//		    if(indexUrlMap == null){
//		    	indexUrlMap = new HashMap();
//		    }
//		    indexUrlMap.put(currentIndexString, serviceUrl);
//		    
//			this.session.setAttribute("index", index); 
//			this.session.setAttribute("indexUrlMap", indexUrlMap);
//		}
//		
//        Cookie[] cookieList = request.getCookies();
//        for(Cookie cookie: cookieList){
//        	if(cookie.getName().equals("ps")){
//        		serviceCookie = cookie;
//        		break;
//        	}
//        }
//        String sc = "name=" + name + "&url=" + serviceUrl;
//        String ps = serviceCookie==null ? sc : serviceCookie.getValue()+"#"+sc;        
//        Cookie psCookie = new Cookie("ps", ps);
//        psCookie.setMaxAge(3*24*60*60); // Set the lifespan of the cookie
//        response.addCookie(psCookie);	// Set value in web.xml?
//        
//        //Preparing a data structure for storing the service in the session
//        Map<String,String> newServiceMap = new HashMap<String,String>();
//        newServiceMap.put("index", currentIndexString);
//        newServiceMap.put("name", name);
//        
//        List<Map> newServiceList = (List<Map>)session.getAttribute("newServiceList");
//        if(newServiceList == null){
//        	newServiceList = new ArrayList<Map>();
//        }
//        
//        //Storing the updated list to the session
//        synchronized(session){
//        	newServiceList.add(newServiceMap);
//        	session.setAttribute("newServiceList", newServiceList);
//        }
//        
//        //Preparing the data structure to send a response to the client
//        JSONObject res = new JSONObject();
//        for(Map service : newServiceList){
//            try {
//            	JSONObject aux = new JSONObject();
//    			aux.put("index", service.get("index"));
//    			aux.put("name", service.get("name"));
//    			res.append("services", service);
//    		} catch (JSONException e) {
//    			System.err.println(e);
//    		}
//        }
//        
//        //Sending response to the client
//		try {
//			PrintWriter out = response.getWriter();
//			out.println(res.toString());
//			out.close();
//		} catch (IOException e) {
//			System.err.println(e);
//		}
//	}
//	
//	/**
//	 * Query the PSICQUIC registry.
//	 * 
//	 * @return
//	 */
	public List<Service> listPSIQUICSercices() {
	    List<Service> list = new ArrayList<Service>(nameToServiceMap.values());
	    // Do a sorting based on name
	    Collections.sort(list, new Comparator<Service>() {
	        public int compare(Service service1, Service service2) {
	            return service1.getName().compareTo(service2.getName());
	        }
	    });
	    return list;
	}
	
//		
	/*
     * Given a list of ReferenceEntity db_ids use the Reactome Java API to
     * retrieve associated Uniprot accession numbers.
     */   
    private Map<String, String> getAccToRefIdMapping(Set<GKInstance> refSeqes) throws Exception {
    	Map<String, String> accToRefidMap = new HashMap<String,String>();
		for (GKInstance refSeq : refSeqes) {
		    if (!refSeq.getSchemClass().isValidAttribute(ReactomeJavaConstants.identifier))
		        continue;
		    String accession = (String) refSeq.getAttributeValue(ReactomeJavaConstants.identifier);
		    accToRefidMap.put(accession, refSeq.getDBID().toString());
		}
		return accToRefidMap;
    }
    
	/**
	 * Get a list of registered PSICQUIC services from the EBI service site.
	 * @throws PsicquicRegistryClientException
	 */
	private void retrievePsicquicServices() throws PsicquicRegistryClientException{
	    nameToServiceMap = new HashMap<String, Service>();
	    
	    PsicquicRegistryClient prc = new DefaultPsicquicRegistryClient();
	    List<ServiceType> list = prc.listServices();
	    
	    for(ServiceType st : list){
	        Service service = new Service(st.getName(), st.getRestUrl());			
	        nameToServiceMap.put(service.getName(), service);
	    }
	}
	
//	
//	private void loadLocalPsiquicRegistry() throws JDOMException, IOException {
//	    logger.info("loadLocalPsuquicRegistry...");
//	    // Load local registry if any
//	    String localRegistryFile = super.getInitParameter("localPsiquicRegistry");
//	    logger.info("localRegistryFile: " + localRegistryFile);
////	    String localRegistryFile = "war/WEB-INF/LocalPsiquicRegistry.xml";
//	    if (localRegistryFile == null)
//	        return; 
//	    File file = new File(localRegistryFile);
//	    if (!file.exists()) {
//	        logger.info(localRegistryFile + " doesn't exist!");
//	        return;
//	    }
//	    SAXBuilder parser = new SAXBuilder();
//	    Document document = parser.build(file);
//	    Element root = document.getRootElement();
//	    Namespace ns = Namespace.getNamespace("http://hupo.psi.org/psicquic/registry");
//	    List<?> children = root.getChildren("service", ns);
//	    for (Iterator<?> it = children.iterator(); it.hasNext();) {
//	        Element elm = (Element) it.next();
//	        String name = elm.getChildTextNormalize("name", ns);
//	        if (name == null || name.length() == 0)
//	            continue;
//	        String restUrl = elm.getChildTextNormalize("restUrl", ns);
//	        if (restUrl == null || restUrl.length() == 0)
//	            continue;
//	        Service service = new Service(name,  restUrl);
//	        indexServiceMap.put(startIndex, service);
//	        Map<String, String> indexNameMap = new HashMap<String, String>();
//	        indexNameMap.put("name", name);
//	        indexNameMap.put("index", startIndex + "");
//	        startIndex ++;
//	    }
//	    logger.info("Total Psiquic services after local registry: " + indexServiceMap.size());
//	}
//	
	private PSICQUICRetriever getPISCQUICRetrieverFromName(String name){
//		PSICQUICRetriever ps;
//		if(serviceIndex < this.startIndex){
//			// If the requested service index is less than startIndex then
//			// the requested service is from the psicquic registry.
//			Service service = this.indexServiceMap.get(serviceIndex);
//			ps = new PSICQUICRetriever(service);
//		}else{
//			// Otherwise, the requested service is from a user added psicquic service
//			Map userServices = (Map) this.session.getAttribute("indexUrlMap");
//			String serviceURL = (String)userServices.get("" + serviceIndex);
//			ps = new PSICQUICRetriever(serviceURL);
//		}
//	    return ps;
	    if (name.startsWith(CustomizedInteractionService.FILE_PREFIX)) { // A label for an user uploaded interaction file
	        CustomizedInteractionService service = new CustomizedInteractionService();
	        service.setTempDir(tempDir);
	        service.setDba(dba);
	        service.setFileName(name);
	        return service;
	    }
	    else if (name.startsWith(CustomizedInteractionService.PSICQUIC_PREFIX)) { // A user submit PSICQUIC
	        CustomizedInteractionService service = new CustomizedInteractionService();
	        String url = service.getRegisteredPSICQUICUrl(name);
	        if (url == null)
	            return null;
	        PSICQUICRetriever retriver = new PSICQUICRetriever(url);
	        return retriver;
	    }
	    Service service = nameToServiceMap.get(name);
	    if (service == null)
	        return null;
	    return new PSICQUICRetriever(service);
	}
	
    @Test
    public void testRetrivePsiquicServices() throws Exception {
        List<Service> serviceList = listPSIQUICSercices();
        System.out.println("Total services: " + serviceList.size());
        for (Service service : serviceList) {
            System.out.println(service.getName() + ": " + service.getRestUrl());
        }
    }
    
    @Test
    public void testQueryInteractionsForPathway() throws Exception {
        MySQLAdaptor dba = new MySQLAdaptor("localhost",
                                            "gk_current_ver42",
                                            "root",
                                            "macmysql01");
        setMySQLAdaptor(dba);
        Long dbId = 73887L; // Death receptor signaling
        dbId = 66212L; // EWAS FASL
        String serviceName = "Reactome-FIs";
        QueryResults results = queryInteractions(dbId, serviceName);
        System.out.println("Results: " + results.getResultList().size());
        for (SimpleQueryResult result : results.getResultList()) {
            System.out.println(result.getRefSeqDBId() + ": " + result.getInteractionList().size());
        }
        
    }
}
