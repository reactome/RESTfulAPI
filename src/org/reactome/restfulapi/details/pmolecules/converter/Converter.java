package org.reactome.restfulapi.details.pmolecules.converter;

import java.util.ArrayList;
import java.util.List;

import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;
import org.reactome.restfulapi.details.pmolecules.model.Molecule;

import org.reactome.restfulapi.details.pmolecules.types.FieldType;
import org.reactome.restfulapi.details.pmolecules.types.FormatType;
import org.reactome.restfulapi.details.pmolecules.types.MoleculeType;

public abstract class Converter {

	protected final String NOT_AVAILABLE = "N/A";
	
	protected ExportConfiguration conf;
	protected ResultContainer data;
	protected List<ExportLine> exportLines = new ArrayList<ExportLine>();
	
	public Converter(ResultContainer data, ExportConfiguration conf){
		super();
		this.conf = conf;
		this.data = data;
		
		try {
			this.convert();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void convert() throws Exception {
		ExportLine firstLine = new ExportLine();
		for (FieldType item : conf.getFields()) {
			ExportItem exportItem = new ExportItem(item, item.getFieldName());
			firstLine.addExportItem(exportItem);
		}
		this.exportLines.add(firstLine);

		for (MoleculeType mt : conf.getMoleculeTypes()) {
			for (Molecule molecule : data.getMolecules(mt).getMolecules()) {
				ExportLine exportLine = new ExportLine();
				for (FieldType ft : conf.getFields()) {
					String value;
					switch (ft) {
					case MOLECULE_TYPE:
						value = molecule.getMoleculeType().getData().getName();
						break;
					case NAME:
						value = molecule.getName();
						break;
					case UNIPROT_ID:
						value = molecule.getUniprotID();
						break;
					case GENE_NAME:
						value = molecule.getGeneName();
						break;
					case CHEBI_ID:
						value = molecule.getChEBIID();
						break;
					default:
						value = NOT_AVAILABLE;
					}
					exportLine.addExportItem(new ExportItem(ft, value));
				}
				this.exportLines.add(exportLine);
			}
		}
	}
	
	public static Converter getConverter(FormatType format, ResultContainer data, ExportConfiguration conf){
		Converter converter;
		switch (format) {
		case TSV:
			converter = new Data2TSV(data, conf);
			break;
		case XML:
			converter = new Data2XML(data, conf);
			break;
		case EXCEL:
			converter = new Data2Excel(data, conf);
			break;
		case CSV:
		default:
			converter = new Data2CSV(data, conf);
			break;
		}
		return converter;
	}

	public abstract String getStringData();
	
	public abstract List<String> getLinesData();
	
	protected String implode(List<String> list, String delim) {
	    String out = "";
	    for(int i=0; i<list.size(); i++) {
	        if(i!=0) { out += delim; }
	        out += list.get(i);
	    }
	    return out;
	}
}
