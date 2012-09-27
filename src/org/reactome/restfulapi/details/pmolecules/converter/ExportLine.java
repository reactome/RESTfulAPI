package org.reactome.restfulapi.details.pmolecules.converter;

import java.util.ArrayList;
import java.util.List;


public class ExportLine {

	List<ExportItem> line = new ArrayList<ExportItem>();

	public ExportLine() {
		super();
	}
	
	public void addExportItem(ExportItem item){
		line.add(item);
	}
	
	public List<ExportItem> getItems(){
		return line;
	}
	
	public List<String> getLineValues(){
		List<String> list = new ArrayList<String>();
		for(ExportItem item : this.line){
			list.add(item.getValue());
		}
		return list;
	}
}
