package org.reactome.restfulapi.details.pmolecules.converter;

import java.util.ArrayList;
import java.util.List;

import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;

public class Data2XML extends Converter {

	public Data2XML(ResultContainer data, ExportConfiguration conf) {
		super(data, conf);
		//In XML does not make sense having the first line
		exportLines.remove(0);
	}

	@Override
	public List<String> getLinesData() {
		List<String> lines = new ArrayList<String>();
		lines.add("<xml>");
		lines.add("<molecules>");
		for(ExportLine exportLine : exportLines){
			String line = "<molecule>";
			for(ExportItem item : exportLine.getItems()){
				String key = item.getKey().getFieldName().replaceAll(" ", "_");
				line += "<" + key + ">" + item.getValue() + "</" + key + ">";
			}
			line += "</molecule>";
			lines.add(line);
		}
		lines.add("</molecules>");
		lines.add("</xml>");
		return lines;
	}

	@Override
	public String getStringData() {
		return implode(this.getLinesData(), "");
	}

}
