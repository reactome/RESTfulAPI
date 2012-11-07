/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.reactome.psicquic.model.SimpleInteractor;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

/**
 * 
 * ServiceManager to be used for treating the STRING PSICQUIC Service data
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
public class STRINGServiceManager extends ServiceManager {

	public STRINGServiceManager() {
	    interactorAccs = new ArrayList<String>();
	    interactorAccs.add("string");
	}

	@Override
	public String getMappingIdDbNames() {
		return "ddbj/embl/genbank,uniprotkb,refseq,chebi,irefindex";
	}
	
	@Override
	protected String getInteractorName(Map<String, List<String>> mapList) {
		for(String acc : interactorAccs){
    		if(mapList.containsKey(acc)){
    			List<String> list = mapList.get(acc);
    			if(!list.isEmpty()) 
    			    return list.get(list.size()-1);
    		}
    	}
		return null;
	}
	
	@Override
	public SimpleInteractor getSimpleInteractor(EncoreInteraction ei, String query) {
		String iA = ei.getInteractorA();    	
    	String iAName = getInteractorName(ei.getOtherInteractorAccsA());
    	
    	String iB = ei.getInteractorB();
    	String iBName = getInteractorName(ei.getOtherInteractorAccsB());
    	
    	double score = getScore(ei);

    	if(iAName==null || iBName==null) return null;
    	
    	SimpleInteractor si;
    	if(iA.equals(query)){
    		si = new SimpleInteractor(iB, iBName, score);
			String iAId = getInteractorID(ei.getOtherInteractorAccsA());
			String iBId = getInteractorID(ei.getOtherInteractorAccsB());
			si.addField("interactorId", iAId);
			si.addField("queryId", iBId);
    	}else{
    		si = new SimpleInteractor(iA, iAName, score);
			String iAId = getInteractorID(ei.getOtherInteractorAccsA());
			String iBId = getInteractorID(ei.getOtherInteractorAccsB());
			si.addField("queryId", iAId);
			si.addField("interactorId", iBId);
    	}
		return si;
	}

	/**
	 * 
	 * @param mapList
	 * @return
	 */
	protected String getInteractorID(Map<String, List<String>> mapList) {
		for(String acc : interactorAccs){
    		if(mapList.containsKey(acc)){
    			List<String> list = mapList.get(acc);
    			if(!list.isEmpty()){
        			return list.get(0);
    			}
    		}
    	}
		return null;
	}

}
