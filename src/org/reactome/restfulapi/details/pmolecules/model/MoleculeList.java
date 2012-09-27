package org.reactome.restfulapi.details.pmolecules.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MoleculeList {

	private String key;
	
	private List<Molecule> list = new ArrayList<Molecule>();

	public MoleculeList() {
	}

	@XmlElement(name="name")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@XmlElement(name="molecules")
	public List<Molecule> getList() {
		return list;
	}

	public void addMolecule(Molecule molecule) {
		list.add(molecule);
	}
}
