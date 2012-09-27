package org.reactome.restfulapi.details.pmolecules.converter;

import java.util.ArrayList;
import java.util.List;

import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;

public abstract class Data2XSV extends Converter {

	protected String delimiter = null;

	public Data2XSV(ResultContainer data, ExportConfiguration conf, String delimiter) {
		super(data, conf);
		this.delimiter = delimiter;  
	}
	
	@Override
	public List<String> getLinesData() {
		List<String> lines = new ArrayList<String>();
		for(ExportLine line : exportLines){
			lines.add(implode(line.getLineValues(), delimiter));
		}
		return lines;
	}

	@Override
	public String getStringData() {
		String out = "";
		for(String line : this.getLinesData()){
			out += line + "\n";
		}
		return out;
	}
}
