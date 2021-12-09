/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class could be easily replaced for a set of SimpleInteractor and just
 * before sending the data to the user, create an array list of SimpleInteractor
 * and later apply the reverse order sort method.
 * 
 * The reason of maintaining this object is because it makes the code easier to
 * understand.  
 *  
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
@XmlRootElement
public class SimpleInteractorList implements Iterable<SimpleInteractor> {

    /**
	 * Contains the list of SimpleInteractor objects
	 */
	private List<SimpleInteractor> interactors = new ArrayList<SimpleInteractor>();
	
    public SimpleInteractorList() {
        
    }

	/**
	 * Adds a SimpleInteractor object to the list only if it is not in yet
	 * 
	 * @param si a SimpleInteractor object
	 */
	public void add(SimpleInteractor si) {
		if (si!=null && !interactors.contains(si))
		    interactors.add(si);
	}

	/**
	 * Returns the size of the list
	 * @return the size of the list
	 */
	public int size(){
		return interactors.size();
	}
	
	/**
	 * Sort the list of SimpleInteractor objects in reverse order
	 */
	public void sortReverseOrder(){
		Collections.sort(interactors, Collections.reverseOrder());
	}
	
	@Override
	public Iterator<SimpleInteractor> iterator() {
		return interactors.iterator();
	}
	
    
    public List<SimpleInteractor> getInteractors() {
        return interactors;
    }

    public void setInteractors(List<SimpleInteractor> list) {
        this.interactors = list;
    }
}
