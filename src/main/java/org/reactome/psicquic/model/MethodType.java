/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.model;

/**
 * 
 * Definition of the possible data retrieval methods implemented in the PSICQUIC
 * proxy
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 *
 */
public enum MethodType {
	DEFAULT(0),			//If method value doesn't match with any known method
	REST(1),			//PSICQUIC REST data retrieval
	SOAP(2),			//Currently not supported because it was not being used
	USER(3);			//Local database (user-submitted data)
	
	private int id;
	
	MethodType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public static MethodType getMethodType(int method){
		for(MethodType mt : MethodType.values()){
			if(mt.getId() == method)
				return mt;
		}
		return DEFAULT;
	}
}
