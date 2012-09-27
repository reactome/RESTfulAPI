package org.reactome.restfulapi.details.pmolecules.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class QueryParams {
	
	Set<MoleculeType> types = new HashSet<MoleculeType>();
	Set<FieldType> fields = new HashSet<FieldType>();
	FormatType format;
	
	public QueryParams(JSONObject json) throws JSONException {
		this.format = FormatType.getFormatType(json.getString("format"));
		
		JSONArray types = json.getJSONArray("types");
		for(int i=0; i<types.length(); ++i){
			this.types.add(MoleculeType.getMoleculeType(types.getString(i)));
		}
		
		JSONArray fields = json.getJSONArray("fields");
		for(int i=0; i<fields.length(); ++i){
			this.fields.add(FieldType.getQueryType(fields.getString(i)));
		}
	}

	public Set<MoleculeType> getTypes() {
		return types;
	}

	public Set<FieldType> getFields() {
		return fields;
	}

	public FormatType getFormat() {
		return format;
	}

	@Override
	public String toString() {
		return "QueryParams [types=" + types + ", fields=" + fields
				+ ", format=" + format + "]";
	}
}
