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
	@XmlElement(name="name")
	private String key;
	
	@XmlElement
	private List<Reference> list = new ArrayList<Reference>();

	public ReferenceList() {
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
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
