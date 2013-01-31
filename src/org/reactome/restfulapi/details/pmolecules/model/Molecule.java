package org.reactome.restfulapi.details.pmolecules.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.gk.model.GKInstance;
import org.reactome.restfulapi.details.pmolecules.types.MoleculeType;

@XmlRootElement
public class Molecule {
    
	private final String NOT_AVAILABLE = "N/A";
	
	@XmlElement
	private Long id;
	@XmlElement
	private String name;
	private MoleculeType moleculeType;
	
	/**
	 * At the moment of developing this class, it seems to have no way of knowing
	 * all the different references types stored in the database, it means that
	 * for each molecule, we will know it by specifically querying  
	 */
	@XmlElement(name="references")
	private List<ReferenceList> references = new ArrayList<ReferenceList>();
	
	public Molecule(){
		super();
	}
	
	public Molecule(Long id, String name, MoleculeType moleculeType) {
		super();
		this.id = id;
		this.name = name;
		this.moleculeType = moleculeType;
	}

	private Set<String> getReferenceTypeKeys(){
		Set<String> keys = new HashSet<String>();
		for(ReferenceList rl : references){
			keys.add(rl.getName());
		}
		return keys;
	}
	
	private ReferenceList getReferenceList(String type){
		for(ReferenceList rl : references){
			if(rl.getName().equals(type))
				return rl;
		}
		return null;
	}
	
	public void addReference(Reference reference){
		if(reference!=null){ 
			String name = reference.getName();
			if(getReferenceTypeKeys().contains(name)){
				getReferenceList(name).addReference(reference);
			}else{
				ReferenceList aux = new ReferenceList();
				aux.setName(name);
				aux.addReference(reference);
				references.add(aux);
			}
		}
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public int getReferencesSize(){
		return references.size();
	}

	public List<String> getReferencesTypes(){
		List<String> list = new ArrayList<String>();
		for(ReferenceList rl : references){
			list.add(rl.getName());
		}
		return list;
	}
	
	public ReferenceList getReferences(String referenceType){
		if(getReferenceTypeKeys().contains(referenceType)){
			for(ReferenceList rl : references){
				if(rl.getName().equals(referenceType))
					return rl;
			}
		}
		return null;
	}
	
	
	public List<ReferenceList> getReferences(){
		return references;
	}
	
	public String getUniprotID(){
		String uniprotID = NOT_AVAILABLE;
		if(moleculeType==MoleculeType.PROTEIN){
			ReferenceList refs = getReferences("UniProt");
			if(!refs.isEmpty()){
				Reference ref = refs.getList().get(0);
				uniprotID = ref.getId();
			}
		}
		return uniprotID;
	}
	
	public String getGeneName(){
		String geneName = NOT_AVAILABLE;
		if(moleculeType==MoleculeType.PROTEIN){
			ReferenceList refs = getReferences("UniProt");
			if(!refs.isEmpty()){
				Reference ref = refs.getList().get(0);
				geneName = ref.getGeneName();
			}
		}
		return geneName;
	}
	
	public String getChEBIID(){
		String uniprotID = NOT_AVAILABLE;
		if(moleculeType==MoleculeType.CHEMICAL){
			ReferenceList refs = getReferences("ChEBI");
			if(!refs.isEmpty()){
				Reference ref = refs.getList().get(0);
				uniprotID = ref.getId();
			}
		}
		return uniprotID;
	}
	
	public MoleculeType getMoleculeType(){
		return this.moleculeType;
	}
	
	public static MoleculeType inferMoleculeType(GKInstance inst){
		MoleculeType moleculeType;
        try{
        	GKInstance ref = (GKInstance) inst.getAttributeValue("referenceEntity");
        	moleculeType = MoleculeType.getMoleculeType(ref.getSchemClass());
        }catch(Exception e){
        	moleculeType = MoleculeType.OTHER;
        }
        return moleculeType;
	}
}
