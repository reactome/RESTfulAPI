/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.service;

import java.util.List;
import java.util.Map;

/**
 * 
 * ServiceManager to be used for treating the IREFINDEX PSICQUIC Service data
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
public class IREFINDEXServiceManager extends DefaultServiceManager{
	
	public IREFINDEXServiceManager(List<String> interactorAccs, String mappingIdDBNames) {
		super(interactorAccs, mappingIdDBNames);
	}

	@Override
	protected String getInteractorName(Map<String, List<String>> mapList) {
		for(String acc : this.interactorAccs){
    		if(mapList.containsKey(acc)){
    			List<String> list = mapList.get(acc);
    			if(!list.isEmpty()) return list.get(list.size()-1);
    		}
    	}
		return null;
	}
}
