/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * After processing a PSI-MITAB line with an interaction, the PSICQUIC Proxy
 * needs to store a simplified data (see the service package) in order to manage
 * a subset of the information.
 * 
 * The aim of the SimpleInteractor class is to store the simplified data.
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
public class SimpleInteractor implements Comparable<SimpleInteractor> {
	/**
	 * Contains the interactor accession value
	 */
	private String accession;
	
	/**
	 * Contains the interactor gene name (note that the name does not make sense
	 * in some cases, but is better to keep it until the javascript client is
	 * rewritten. 
	 */
	private String genename;
	
	/**
	 * Contains the score value provided by micluster package
	 */
	private Double score;
	
	/**
	 * Container of extra fields needed in some services treatment
	 */
	private Map<String, String> extraFields = new HashMap<String, String>();
	
	public SimpleInteractor() {
	    
	}
	
	/**
	 * Creates a new SimpleInteractor instance from the following data
	 * 
	 * @param accession the interactor accession value
	 * @param genename the interactor gene name
	 * @param score the interactor score value
	 */
	public SimpleInteractor(String accession, String genename, double score) {
		this.accession = accession;
		this.genename = genename != null ? genename : "";
		this.score = score;
	}
	
	/**
	 * Returns the interactor accession value
	 * @return the interactor accession value
	 */
	public String getAccession() {
		return accession;
	}

	/**
	 * Set the interactor accession value
	 * @param accession the interactor accession value
	 */
	public void setAccession(String accession) {
		this.accession = accession;
	}

	/**
	 * Returns the interactor gene name
	 * @return the interactor gene name
	 */
	public String getGenename() {
		return genename;
	}

	/**
	 * Set the interactor gene name
	 * @param genename the interactor gene name
	 */
	public void setGenename(String genename) {
		this.genename = genename;
	}

	/**
	 * Returns the interactor score value
	 * @return the interactor score value
	 */
	public Double getScore() {
		return score;
	}

	/**
	 * Set the interactor score value
	 * @param score the interactor score value
	 */
	public void setScore(Double score) {
		this.score = score;
	}

	/**
	 * Add an extra field in the result data set
	 * 
	 * @param name extra field label name
	 * @param content extra field content value
	 */
	public void addField(String name, String content){
		this.extraFields.put(name, content);
	}
	
	/**
	 * Returns the extra field content value if it exists (if not returns null)
	 * 
	 * @param name extra field label name
	 * @return extra field content value (null if not exists)
	 */
	public String getFieldValue(String name){
		if(this.extraFields.containsKey(name))
			return this.extraFields.get(name);
		else
			return null;
	}

	@Override
	public int compareTo(SimpleInteractor arg0) {
		//only the score value is used for the comparison
		return this.score.compareTo(arg0.getScore());
	}

	@Override
	public boolean equals(Object obj) {
		SimpleInteractor si = (SimpleInteractor) obj;
		return this.accession.equals(si.getAccession());
	}

}
