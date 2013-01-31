package org.reactome.restfulapi.details.pmolecules.types;

import javax.xml.bind.annotation.XmlRootElement;

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

	@XmlRootElement(name="moleculeType")
	public class Data {
		private String name;
		
		Data(String name){
			this.name = name;
		}
		
		public String getName(){
			return this.name;
		}
	}
	
	private Data data;
	
	MoleculeType(String name) {
		this.data = new Data(name);
	}

	public Data getData(){
		return this.data;
	}

	public static MoleculeType getMoleculeType(String name){
		for(MoleculeType mt : MoleculeType.values())
			if(mt.data.getName().equals(name)) return mt;
		
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
