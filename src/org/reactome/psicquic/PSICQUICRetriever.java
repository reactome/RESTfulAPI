package org.reactome.psicquic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hupo.psi.mi.psicquic.wsclient.PsicquicSimpleClient;
import org.reactome.psicquic.model.QueryResults;
import org.reactome.psicquic.model.SimpleInteractor;
import org.reactome.psicquic.model.SimpleInteractorList;
import org.reactome.psicquic.model.SimpleQueryResult;
import org.reactome.psicquic.service.DefaultServiceManager;
import org.reactome.psicquic.service.Service;
import org.reactome.psicquic.service.ServiceManager;

import psidev.psi.mi.tab.io.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.score.InteractionClusterScore;

public class PSICQUICRetriever {
    
    private String URL;
    private String serviceName;
    private ServiceManager sm;
    private List<String> userData = null;
    
    /**
     * The default constructor is used for subclassing.
     */
    protected PSICQUICRetriever() {
        
    }
    public PSICQUICRetriever(List<String> userData) {
        this.URL = "User data";
        this.serviceName = "User data";
        this.sm = new DefaultServiceManager();
        this.userData = userData;
    }
    
    public PSICQUICRetriever(Service service) {
        this.URL = service.getRestUrl();
        this.serviceName = service.getName();
        this.sm = ServiceManager.getServiceManager(this.serviceName);
    }
    
    public PSICQUICRetriever(String URL) {
        this.URL = URL;
        this.serviceName = "";
        this.sm = new DefaultServiceManager();
    }
    
    public QueryResults getDataFromRest(Map<String, String> accessionToRefSeqId) throws IOException {
        return getDataFromRest(accessionToRefSeqId, 
                               -1);
    }
    
    @SuppressWarnings("rawtypes")
    private QueryResults getDataFromRest(Map<String, String> accessionToRefId,
                                         int maxResults) throws IOException {
        List<SimpleQueryResult> sqrList = new ArrayList<SimpleQueryResult>();
        
        InteractionClusterScore ics = new InteractionClusterScore();
        String restQuery = StringUtils.join(accessionToRefId.keySet().iterator(), " OR ");
        PsicquicSimpleClient client = new PsicquicSimpleClient(this.URL);
        InputStream result = client.getByQuery(restQuery);
        PsimiTabReader reader = new psidev.psi.mi.tab.PsimiTabReader();
        Iterator<BinaryInteraction> iterator = reader.iterate(result);
        ics.setBinaryInteractionIterator(iterator);
        
        ics.setMappingIdDbNames(this.sm.getMappingIdDbNames());
        ics.runService();
        
	    // DEV-885 Note: the score is still retained at this point but it was
	    // getting upon conversion to EncoreInteraction objects

        // Retrieve results
        Map<Integer, EncoreInteraction> interactionMapping = ics.getInteractionMapping();
        
        Map<String, SimpleInteractorList> querySIL = new HashMap<String, SimpleInteractorList>();

        // DEV-1046  psiquicInteractions NOT working.
        // It was failing silently right here...
        try { 
        	for(EncoreInteraction ei : interactionMapping.values() ){
        		String queryA = ei.getInteractorA();
        		addResultToQuerySIL(ei, queryA, accessionToRefId, querySIL);

        		String queryB = ei.getInteractorB();
        		addResultToQuerySIL(ei, queryB, accessionToRefId, querySIL);
        	}
        }
        catch (Throwable e) {
        	// ..and I am not sure why!
        	// System.err.println("I have no idea why I am here");  
        }
        
        for(String queryAux : querySIL.keySet()){
            String refid = accessionToRefId.get(queryAux);
            SimpleInteractorList sil = querySIL.get(queryAux);
            SimpleQueryResult sqr = new SimpleQueryResult(queryAux,refid,sil);
            sqr.setMaxResults(maxResults);
            sqrList.add(sqr);
        }
        return new QueryResults(sqrList);
    }
    
    protected List<String> getInteractionListFromRest(Map<String, String> accessionToRefEntityId) throws IOException {
        //if the object contains USER data, this method will return that without
        //querying any service and without taken into account the interaction list
        if (this.userData!=null) 
            return this.userData;
        Set<String> accessions = accessionToRefEntityId.keySet();
        String restQuery = StringUtils.join(accessions, " OR ");
        //restQuery = "identifier:(" + restQuery + ")";
        
        //		System.err.println("PSICQUICRetriever.getInteractionListFromRest: Service Name: " + this.serviceName);
        //		System.err.println("PSICQUICRetriever.getInteractionListFromRest: Service URL: " + this.URL);
        //		System.err.println("PSICQUICRetriever.getInteractionListFromRest: Query: " + restQuery);
        //		
        List<String> list = new ArrayList<String>();
        
        PsicquicSimpleClient client = new PsicquicSimpleClient(this.URL);
        InputStream result = client.getByQuery(restQuery);
        BufferedReader in = new BufferedReader(new InputStreamReader(result));
        
        String line;	        
        while ((line = in.readLine()) != null)
            list.add(line);
        in.close();
        return list;
    }
    
    /**
     * Adds a SimpleInteractor to the corresponding SimpleInteractorList if the
     * candidate query turns out to be valid taking into account the queryRefid
     * key set as valid query identifiers
     * 
     * @param ei
     * @param query
     * @param accessionToRefSeqId
     * @param querySIL
     */
    private void addResultToQuerySIL(EncoreInteraction ei, 
    		String query,
    		Map<String, String> accessionToRefSeqId,
    		Map<String, SimpleInteractorList> querySIL){
    	// Reactome use ids only. Some databases may attached db name before the ids.
    	// Need a little parsing here
    	String id = query;
    	if (query.contains(":")) {
    		int index = query.indexOf(":");
    		id = query.substring(index + 1);
    	}
    	if(accessionToRefSeqId.keySet().contains(id)) {
    		SimpleInteractor si = this.sm.getSimpleInteractor(ei, query);

    		// Now, to get those scores (DEV-855)
    		List<Confidence> confidenceScores = ei.getConfidenceValues();
    		for(Confidence confidenceScore:confidenceScores){
    			if(confidenceScore.getType().equalsIgnoreCase("miscore")){
    				String score = confidenceScore.getValue();
    				if (score != null) {
    					si.setScore(Double.parseDouble(score));
    				}
    			}
    		}
    		
    		// Next lines get an existing SimpleInteractorList from querySIL or
    		// create a new one (adding it to querySIL for future use)
    		SimpleInteractorList sil;
    		if(querySIL.containsKey(query)){
    			sil = querySIL.get(query);
    		}else{
    			sil = new SimpleInteractorList();
    			querySIL.put(query, sil);
    		}

    		sil.add(si);
    	}

    }	
    
    /**
     * Return interactions in a raw text.
     * @param accessionToRefEntId
     * @return
     * @throws IOException
     */
    public String exportInteractions(Map<String, String> accessionToRefEntId) throws IOException {
        List<String> lines = getInteractionListFromRest(accessionToRefEntId);
        return StringUtils.join(lines, '\n');
    }
    
}
