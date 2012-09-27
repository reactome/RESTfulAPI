package org.reactome.restfulapi.details.pmolecules.converter;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;


public class Data2Excel extends Converter{


	public Data2Excel(ResultContainer data, ExportConfiguration conf) {
		super(data, conf);
	}
	
	public HSSFWorkbook getWorkbook(){
		HSSFWorkbook workbook = new HSSFWorkbook(); 
		HSSFSheet sheet = workbook.createSheet("Participating molecules"); 

		int rowNum = 0; 
		for(ExportLine exportLine : exportLines){
			HSSFRow row = sheet.createRow(rowNum++);
			int colNum = 0;
			for(ExportItem item : exportLine.getItems()){
				HSSFRichTextString value = new HSSFRichTextString(item.getValue());
				row.createCell(colNum++).setCellValue(value);
			}
		}
		return workbook;
	}

	@Override
	public List<String> getLinesData() {
		return null;
	}

	@Override
	public String getStringData() {
		return null;
	}
	
	
	
}
