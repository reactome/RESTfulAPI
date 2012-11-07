package org.reactome.psicquic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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

import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.Interactor;
import psidev.psi.mi.tab.model.builder.MitabDocumentDefinition;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;
import uk.ac.ebi.enfin.mi.cluster.score.InteractionClusterScore;

public class PSICQUICRetriever {
    
	private String URL;
	private String serviceName;
	private ServiceManager sm;
	private List<String> userData = null;
	
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
						
	public QueryResults getDataFromRest(Map<String, String> queryRefid) throws IOException {
		return getDataFromRest(queryRefid, 
		                       -1);
	}
	
	@SuppressWarnings("unchecked")
	public QueryResults getDataFromRest(Map<String, String> accessionToRefSeqId, int maxResults) throws IOException {
		InteractionClusterScore ics = new InteractionClusterScore();
		MitabDocumentDefinition mdd = new MitabDocumentDefinition();
					
		for(String line : getInteractionListFromRest(accessionToRefSeqId.keySet())){
			if (line.isEmpty())
				continue;
			BinaryInteraction<Interactor> bi = mdd.interactionFromString(line);
		    ics.addBinaryInteraction(bi);
		}
		
		ics.setMappingIdDbNames(this.sm.getMappingIdDbNames());
		ics.runService();
		
		// Retrieve results
		Map<Integer, EncoreInteraction> interactionMapping = ics.getInteractionMapping();

		String query;
		Map<String, SimpleInteractorList> querySIL = new HashMap<String, SimpleInteractorList>();
		for(EncoreInteraction ei : interactionMapping.values() ){           	
			query = ei.getInteractorA();
			addResultToQuerySIL(ei, query, accessionToRefSeqId, querySIL);
			
			query = ei.getInteractorB();
			addResultToQuerySIL(ei, query, accessionToRefSeqId, querySIL);
		}
		
		List<SimpleQueryResult> sqrList = new ArrayList<SimpleQueryResult>();
		for(String queryAux : querySIL.keySet()){
			String refid = accessionToRefSeqId.get(queryAux);
			SimpleInteractorList sil = querySIL.get(queryAux);
			SimpleQueryResult sqr = new SimpleQueryResult(queryAux,refid,sil);
			sqr.setMaxResults(maxResults);
			sqrList.add(sqr);
		}        
        return new QueryResults(sqrList);
	}
	
	private List<String> getInteractionListFromRest(Set<String> accessionList) throws IOException {
	    //if the object contains USER data, this method will return that without
	    //querying any service and without taken into account the interaction list
	    if (this.userData!=null) 
	        return this.userData;
	    
	    String restQuery = StringUtils.join(accessionList, " OR ");
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
	 * @param queryRefid
	 * @param querySIL
	 */
	private void addResultToQuerySIL(EncoreInteraction ei, String query,
									 Map<String, String> queryRefid,
									 Map<String, SimpleInteractorList> querySIL){
		
		if(queryRefid.keySet().contains(query)){
    		SimpleInteractor si = this.sm.getSimpleInteractor(ei, query);

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
}
