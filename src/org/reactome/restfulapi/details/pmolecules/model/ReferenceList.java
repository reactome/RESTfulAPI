package org.reactome.restfulapi.details.pmolecules.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReferenceList {
	private String name;
	
	@XmlElement
	private List<Reference> list = new ArrayList<Reference>();

	public ReferenceList() {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Reference> getList() {
		return list;
	}

	public void addReference(Reference reference) {
		list.add(reference);
	}
	
	public boolean isEmpty(){
		return list.isEmpty();
	}
}
