/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.model;

/**
 * 
 * Definition of the possible actions implemented in the PSICQUIC proxy
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 *
 */
public enum ActionType {
	DEFAULT(""),		//If action value doesn't match with any known action
	ALL_INTERACTIONS("pi"),			//Retrieve interactions for all proteins in pathway
	EXPORT("ex"),			//Export of PSIMI-TAB data for all protein interactions
	EXPORT_SUBSET("exi"),			//Export of PSIMI-TAB data for a set of protein interactions
	NEW_SERVICE("newps"),		//Add a new PSICQUIC Service
	LIST_SERVICES("reg");			//List all the available PSICQUIC Services

	private String id;
	
	ActionType(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

	public static ActionType getActionType(String action){
		if(action==null) return DEFAULT;
		for(ActionType at : ActionType.values()){
			if(at.getId().equals(action))
				return at;
		}
		return DEFAULT;
	}
}
