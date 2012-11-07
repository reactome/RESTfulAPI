/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.reactome.psicquic.model.SimpleInteractor;

import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

/**
 * 
 * ServiceManager to be used by default for treating a PSICQUIC Service data
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
public class DefaultServiceManager extends ServiceManager {
	private String mappingIdDBNames = "uniprotkb,ddbj/embl/genbank,refseq,chebi,irefindex";

	public DefaultServiceManager() {
	    String[] dbNames = new String[] {
	            "uniprot", 
	            "uniprotkb", 
	            "dip", 
	            "embl", 
	            "chembl"
	    };
	    interactorAccs = Arrays.asList(dbNames);
	}
	
	/**
	 * Default manager can deal with different interactor Accs and mappingIdDBNames,
	 * so this constructor allow user to instantiate it without writing a new
	 * class
	 * 
	 * @param interactorAccs
	 * @param mappingIdDBNames
	 */
	public DefaultServiceManager(List<String> interactorAccs, String mappingIdDBNames) {
		this.interactorAccs = interactorAccs;
		this.mappingIdDBNames = mappingIdDBNames;
	}
	
	@Override
	public String getMappingIdDbNames() {
		return this.mappingIdDBNames;
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
    	}else{
    		si = new SimpleInteractor(iA, iAName, score);
    	}
		return si;
	}
	
	@Override
	protected String getInteractorName(Map<String, List<String>> mapList) {
		for(String acc : this.interactorAccs){
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
