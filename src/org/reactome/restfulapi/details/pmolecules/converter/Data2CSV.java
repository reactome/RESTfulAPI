package org.reactome.restfulapi.details.pmolecules.converter;


import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;

public class Data2CSV extends Data2XSV{
	
	public Data2CSV(ResultContainer data, ExportConfiguration conf) {
		super(data, conf, ",");
	}

}
