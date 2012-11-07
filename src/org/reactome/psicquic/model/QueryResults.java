/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Data structure to store the results of a query to the PSICQUIC Proxy.
 * Provides the functionality of converting the result to JSON Format.
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
@XmlRootElement
public class QueryResults {
	/**
	 * Contains a list of SimpleQueryResult with the result of each queried item
	 */
	private List<SimpleQueryResult> resultList = new ArrayList<SimpleQueryResult>();

	/**
	 * Contains a message to be sent to the client (if something went wrong)
	 */
	private String errorMessage = null;

	/**
	 * Creates an Empty new QueryResults instance
	 */
	public QueryResults() {
	}

	/**
	 * Creates a new QueryResults instance from a list of SimpleQueryResults
	 * 
	 * @param resultList
	 *            list of SimpleQueryResults
	 */
	public QueryResults(List<SimpleQueryResult> resultList) {
		this.resultList = resultList;
	}

	/**
	 * Creates a new QueryResults instance containing an error message
	 * 
	 * @param errorMessage
	 *            error message to be sent to the client
	 */
	public QueryResults(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * Add a new SimpleQueryResult to the list of results
	 * 
	 * @param result
	 *            a new SimpleQueryResult to be added the list of results
	 */
	public void addSimpleQueryResult(SimpleQueryResult result) {
		this.resultList.add(result);
	}

	/**
	 * Returns the list of SimpleQueryResults
	 * 
	 * @return the list of SimpleQueryResults
	 */
	public List<SimpleQueryResult> getResultList() {
		return this.resultList;
	}
	
	public void setResultList(List<SimpleQueryResult> list) {
	    this.resultList = list;
	}

	/**
	 * If there is an error will return a message (null if there is not an error
	 * message stored in the object)
	 * 
	 * @return an error message (null if there is not an error message)
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Set the error message to be sent to the client
	 * 
	 * @param errorMessage
	 *            the error message to be sent to the client
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * If there is an error message, returns true
	 * 
	 * @return True if there is an error message (False if not)
	 */
	public boolean success() {
		return this.errorMessage == null;
	}
}
