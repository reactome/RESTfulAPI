package org.reactome.restfulapi.details.pmolecules.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.reactome.restfulapi.details.pmolecules.types.FieldType;
import org.reactome.restfulapi.details.pmolecules.types.MoleculeType;

public class ExportConfiguration {

	private Set<MoleculeType> moleculeTypes;
	private Set<FieldType> fields;

	public ExportConfiguration() {
		this.moleculeTypes = new HashSet<MoleculeType>();
		this.fields = new HashSet<FieldType>();
	}

	public ExportConfiguration(Set<MoleculeType> moleculeTypes,
			Set<FieldType> fields) {
		super();
		this.moleculeTypes = moleculeTypes;
		this.fields = fields;
	}

	/**
	 * Return an ordered list of the contained molecule types to export
	 * 
	 * @return an ordered list of the contained molecule types to export
	 */
	public List<MoleculeType> getMoleculeTypes() {
		List<MoleculeType> list = new ArrayList<MoleculeType>();
		for (MoleculeType mt : MoleculeType.values()) {
			if (moleculeTypes.contains(mt))
				list.add(mt);
		}

		return list;
	}

	/**
	 * Return an ordered list of the contained fields to export
	 * 
	 * @return an ordered list of the contained fields to export
	 */
	public List<FieldType> getFields() {
		List<FieldType> list = new ArrayList<FieldType>();
		for (FieldType mt : FieldType.values()) {
			if (fields.contains(mt))
				list.add(mt);
		}
		return list;
	}

	public void addMoleculeType(MoleculeType moleculeType) {
		if (moleculeType != null) {
			this.moleculeTypes.add(moleculeType);
		}
	}

	public void addField(FieldType fieldType) {
		if (fieldType != null) {
			this.fields.add(fieldType);
		}
	}
}
