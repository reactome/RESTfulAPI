package org.reactome.restfulapi.details.pmolecules.types;

import org.gk.schema.SchemaClass;

/**
 * 
 * @author amundo
 *
 */
public enum MoleculeType implements Comparable<MoleculeType> {

	PROTEIN("Proteins"),
	CHEMICAL("Chemical compounds"),
	OTHER("Others");

	private String name;
	
	MoleculeType(String name) {
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public static MoleculeType getMoleculeType(String name){
		for(MoleculeType mt : MoleculeType.values())
			if(mt.getName().equals(name)) return mt;
		
		return null;
	}
	
	public static MoleculeType getMoleculeType(SchemaClass schemClass){		
		MoleculeType res;
		if(schemClass.isa("ReferenceGeneProduct")){
			res = MoleculeType.PROTEIN;
		}else if(schemClass.isa("ReferenceMolecule")){
			res = MoleculeType.CHEMICAL;
		}else{
			res = MoleculeType.OTHER;
		}
		return res;
	}
}
