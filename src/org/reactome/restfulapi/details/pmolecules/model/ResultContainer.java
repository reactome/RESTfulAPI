package org.reactome.restfulapi.details.pmolecules.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.reactome.restfulapi.details.pmolecules.types.MoleculeType;

@XmlRootElement(name="participatingMolecules")
public class ResultContainer {
	
	/**  */
	private boolean success = true;
	
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
	public boolean getSuccess() {
		return this.success;
	}

	@XmlElement
	public String getErrorMessage() {
		return errorMessage;
	}

	@XmlElement(name="moleculeTypes")
	public List<MoleculeList> getParticipatingMolecules(){
		return molecules;
	}
	
	private Set<String> getMoleculesTypeKeys(){
		Set<String> keys = new HashSet<String>();
		for(MoleculeList ml : molecules){
			keys.add(ml.getKey());
		}
		return keys;
	}
	
	private MoleculeList getMoleculeList(String type){
		for(MoleculeList ml : molecules){
			if(ml.getKey().equals(type))
				return ml;
		}
		return null;
	}
	
	public List<MoleculeType> getMoleculesTypes(){
		
		List<MoleculeType> typeList = new ArrayList<MoleculeType>();
		for(String key : getMoleculesTypeKeys()){
			typeList.add(MoleculeType.getMoleculeType(key));
		}
        Collections.sort(typeList);
        return typeList;
	}
	
	@XmlElement(name="moleculesNum", type=Integer.class)
	public Integer getMoleculesNumber(){
		Integer size = 0;
		for(String mt : getMoleculesTypeKeys()){
			size += getMoleculeList(mt).getList().size();
		}
		return size;
	}

	public MoleculeList getMolecules(MoleculeType moleculeType) {
		if(getMoleculesTypeKeys().contains(moleculeType.getName())){
			for(MoleculeList ml : molecules){
				if(ml.getKey().equals(moleculeType.getName()))
					return ml;
			}
		}
		return null;
	}
	
	public void addMolecule(MoleculeType type, Molecule molecule){
		if(type==null || molecule==null) return;
		
		if(getMoleculesTypeKeys().contains(type.getName())){
			getMolecules(type).addMolecule(molecule);
		}else{
			MoleculeList aux = new MoleculeList();
			aux.setKey(type.getName());
			aux.addMolecule(molecule);
			molecules.add(aux);
		}
	}
}
