/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.service;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Container for a single service identification data
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 *
 */
@XmlRootElement
public class Service {
	/**
	 * Contains the service name
	 */
	private String name;
	
	/**
	 * Contains the service REST URL
	 */
	private String restUrl;
	
	public Service() {
	}

	/**
	 * Creates a new Service instance from the following data
	 * 
	 * @param name the service name
	 * @param restUrl the service REST URL
	 */
	public Service(String name, String restUrl) {
		super();
		this.name = name;
		this.restUrl = restUrl;
	}

	/**
	 * Set the service name
	 * @param name the service name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the service name
	 * @return the service name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the service REST URL
	 * @param restUrl the service REST URL
	 */
	public void setRestUrl(String restUrl) {
		this.restUrl = restUrl;
	}

	/**
	 * Returns the service REST URL
	 * @return the service REST URL
	 */
	public String getRestUrl() {
		return restUrl;
	}

	@Override
	public String toString() {
		return "Service [name=" + name + ", restUrl=" + restUrl + "]";
	}
}