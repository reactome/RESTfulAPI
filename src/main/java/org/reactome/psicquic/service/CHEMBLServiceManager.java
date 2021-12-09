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
 * ServiceManager to be used for treating the CHEMBL PSICQUIC Service data
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
public class CHEMBLServiceManager extends ServiceManager {

	public CHEMBLServiceManager() {
	    interactorAccs = new ArrayList<String>();
	    interactorAccs.add("chembl");
	}

	@Override
	public String getMappingIdDbNames() {
		return "ddbj/embl/genbank,uniprotkb,refseq,chebi,irefindex";
	}

	@Override
	public SimpleInteractor getSimpleInteractor(EncoreInteraction ei, String query) {
		String iA = ei.getInteractorA();    	
    	String iAName = getInteractorName(ei.getOtherInteractorAccsA());
    	
    	String iAGeneName = "";
    	String iAAccession = "";
    	List<String> iAExtra = ei.getOtherInteractorAccsA().get("unknown");
    	for(String item : iAExtra){
    		if(item.contains("InChI")){
    			String[] values = item.split("/");
    			iAGeneName = values[1];
    	    	iAAccession = values[2];
    	    	break;
    		}
    	}
    	
    	String iB = ei.getInteractorB();
    	String iBName = getInteractorName(ei.getOtherInteractorAccsB());
    	
    	String iBGeneName = "";
    	String iBAccession = "";
    	List<String> iBExtra = ei.getOtherInteractorAccsB().get("unknown");
    	for(String item : iAExtra){
    		if(item.contains("InChI")){
    			String[] values = item.split("/");
    			iBGeneName = values[1];
    	    	iBAccession = values[2];
    	    	break;
    		}
    	}

    	double score = getScore(ei);

    	if (iAName==null || iBName==null) 
    	    return null;
    	
    	SimpleInteractor si;
    	if(iA.equals(query)){
    		si = new SimpleInteractor(iBAccession, iBGeneName, score);
    		si.addField("cid",  iBName);
    		si.addField("chemblid", iBName);
    	}else{
    		si = new SimpleInteractor(iAAccession, iAGeneName, score);
    		si.addField("cid", iAName);
    		si.addField("chemblid", iAName);
    	}
		return si;
	}
	
	@Override
	protected String getInteractorName(Map<String, List<String>> mapList) {
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
