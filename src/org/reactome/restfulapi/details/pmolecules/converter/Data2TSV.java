package org.reactome.restfulapi.details.pmolecules.converter;

import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;

public class Data2TSV extends Data2XSV{
	
	public Data2TSV(ResultContainer data, ExportConfiguration conf) {
		super(data, conf, "\t");
	}

}
