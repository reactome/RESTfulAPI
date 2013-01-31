package org.reactome.restfulapi.details.pmolecules.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MoleculeList {

	private String name;
	
	private List<Molecule> list = new ArrayList<Molecule>();

	public MoleculeList() {
	}

	public String getName() {
		return name;
	}

	public void setName(String key) {
		this.name = key;
	}

	public List<Molecule> getMolecules() {
		return list;
	}

	public void addMolecule(Molecule molecule) {
		list.add(molecule);
	}
}
