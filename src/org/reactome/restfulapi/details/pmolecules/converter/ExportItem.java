package org.reactome.restfulapi.details.pmolecules.converter;

import org.reactome.restfulapi.details.pmolecules.types.FieldType;

public class ExportItem {

	private FieldType key;
	private String value;
	
	public ExportItem(FieldType key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	public FieldType getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}
}
