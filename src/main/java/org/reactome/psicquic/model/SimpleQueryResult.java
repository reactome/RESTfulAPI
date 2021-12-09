/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Data structure to store the results of a single query.
 * Provides the functionality of converting the result to JSON Format.
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
@XmlRootElement
public class SimpleQueryResult {
	/**
	 * Contains the original query value
	 */
	private String query;
	
	/**
	 * Contains the list of SimpleInteractor objects associated to the query
	 */
	private SimpleInteractorList interactionList;
	
	/**
	 * Contains the number of results to be provided (-1 means all the results)
	 */
	private int maxResults = -1;
	
	/**
	 * Contains the original referenceId value (before translation)
	 */
	private String refSeqDBId;
	
	public SimpleQueryResult() {
	}
	
	/**
	 * Creates a new SimpleQueryResult instance from the following data
	 * 
	 * @param query the original query value
	 * @param refid the original referenceId value
	 * @param sil the list of SimpleInteractor objects associated to the query
	 */
	public SimpleQueryResult(String query, String refid, SimpleInteractorList sil) {
		this.query = query;
		this.refSeqDBId = refid;
		this.interactionList = sil;
	}
	
	/**
	 * Sets the maximum number of results to be provided to the client
	 * @param number number of results to be provided to the client (-1 means
	 * 				 all the results)
	 */
	public void setMaxResults(int number){
		this.maxResults = number;
	}
	
	/**
	 * Reset the number of results to be provided to all the results
	 */
	public void resetMaxResults(){
		this.maxResults = -1;
	}

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public SimpleInteractorList getInteractionList() {
        return interactionList;
    }

    public void setInteractionList(SimpleInteractorList interactionList) {
        this.interactionList = interactionList;
    }

    public String getRefSeqDBId() {
        return refSeqDBId;
    }

    public void setRefSeqDBId(String refSeqDBId) {
        this.refSeqDBId = refSeqDBId;
    }

    public int getMaxResults() {
        return maxResults;
    }
	
}
