package org.reactome.restfulapi.details.pmolecules.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;

import org.reactome.restfulapi.details.pmolecules.types.MoleculeType;

public class ResultContainer {
	
	/**  */
	private Boolean success = true;
	
	/**  */
	private String errorMessage;
	
	/**  */
	private List<MoleculeList> molecules = new ArrayList<MoleculeList>();
		
	public ResultContainer() {
		super();
	}
	
	public void setErrorMessage(String errorMessage) {
		this.success = false;
		this.molecules = new ArrayList<MoleculeList>();
		this.errorMessage = errorMessage;
	}
	
	@XmlElement(type=Boolean.class)
	public Boolean getSuccess() {
		return this.success;
	}

	@XmlElement
	public String getErrorMessage() {
		return errorMessage;
	}

	public List<MoleculeList> getParticipatingMolecules(){
		return molecules;
	}
	
	private Set<String> getMoleculesTypeKeys(){
		Set<String> keys = new HashSet<String>();
		for(MoleculeList ml : molecules){
			keys.add(ml.getName());
		}
		return keys;
	}
	
	private MoleculeList getMoleculeList(String type){
		for(MoleculeList ml : molecules){
			if(ml.getName().equals(type))
				return ml;
		}
		return null;
	}
	
	public List<MoleculeType> getMoleculeTypes(){
		
		List<MoleculeType> typeList = new ArrayList<MoleculeType>();
		for(String key : getMoleculesTypeKeys()){
			typeList.add(MoleculeType.getMoleculeType(key));
		}
        //Collections.sort(typeList);
        return typeList;
	}
	
	public Integer getMoleculesNumber(){
		Integer size = 0;
		for(String mt : getMoleculesTypeKeys()){
			size += getMoleculeList(mt).getMolecules().size();
		}
		return size;
	}

	public MoleculeList getMolecules(MoleculeType moleculeType) {
		if(getMoleculesTypeKeys().contains(moleculeType.getData().getName())){
			for(MoleculeList ml : molecules){
				if(ml.getName().equals(moleculeType.getData().getName()))
					return ml;
			}
		}
		return null;
	}
	
	public void addMolecule(MoleculeType type, Molecule molecule){
		if(type==null || molecule==null) return;
		
		if(getMoleculesTypeKeys().contains(type.getData().getName())){
			getMolecules(type).addMolecule(molecule);
		}else{
			MoleculeList aux = new MoleculeList();
			aux.setName(type.getData().getName());
			aux.addMolecule(molecule);
			molecules.add(aux);
		}
	}
}
