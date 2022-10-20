package com.dsinnovators.keyword.driven.commons;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;

@Slf4j
public class ExcelUtils {

	public static final int TEST_CASE_NAME = 0;
	public static final int LABEL_COLUMN = 1;
	public static final int KEYWORD_COLUMN = 2;
	public static final int REGISTRY_KEY = 3;
	public static final int ELEMENT_LOCATOR = 4;
	public static final int ASSERTION_TYPE = 5;
	public static final int VALUE_COLUMN = 6;
	public static final int ASSERTION_WITH = 7;
	public static final int ASSERTION_ACTION = 8;
	public static final int TIMEOUT_COLUMN = 9;
	public static final int EXPECTED_RESULT_COLUMN = 10;
	public static final int SEED_DATA = 11;
	public static final int COMMENTS_COLUMN = 12;

	private static XSSFSheet ExcelWSheet;
	private static XSSFWorkbook ExcelWBook;
	private static XSSFCell Cell;


	//This method is to set the File path and to open the Excel file
	//Pass Excel Path and SheetName as Arguments to this method
	public static void setExcelFile(String Path,String SheetName) throws Exception {
		log.info("IMLOG:: Path "+Path+" SheetName "+SheetName);
		File excelFile = new File(Path);
		if (!excelFile.exists()) {
			throw new Exception("Excel file not found.!!!");
		}else{
			log.info("Excel file found.");
		}
		FileInputStream ExcelFile = new FileInputStream(Path);
		// log.info("IMLOG2:: Path "+Path+" SheetName "+SheetName);
		ExcelWBook = new XSSFWorkbook(ExcelFile);
		// log.info("IMLOG3:: Path "+Path+" SheetName "+SheetName);
		ExcelWSheet = ExcelWBook.getSheet(SheetName);
	}

	public static int getNumberOfRows() throws Exception{
		int numberOfRows = 0;
		if(ExcelWSheet!=null) {
			Iterator<Row> rowIter = ExcelWSheet.rowIterator();
			while (rowIter.hasNext()) {
				numberOfRows++;
				Row row = rowIter.next();
				//Row row = rowIterator.next();
			}
		}else{
			throw new Exception("Sheet not found.!!!");
		}
		return numberOfRows;
	}
	public static String getCellStringData(int RowNum, int ColNum) throws Exception{
		if(ExcelWSheet!=null) {
			Cell = ExcelWSheet.getRow(RowNum).getCell(ColNum);
			if (Cell == null) {
				return null;
			}
			switch (Cell.getCellType()) {
				case 0:
					//return Cell.getStringCellValue();
					System.out.println("Cell info = " + Cell.getRawValue());
					return String.valueOf(Cell.getRawValue());
				case 1:
					System.out.println("Cell info = " + Cell.getRawValue());
					return Cell.getStringCellValue();
			}
		}else{
			throw new Exception("Sheet not found.!!!");
		}
		return null;
	}

	public static Number getCellNumericData(int RowNum, int ColNum) throws Exception{
		Cell = ExcelWSheet.getRow(RowNum).getCell(ColNum);
		return Cell.getNumericCellValue();

	}


	public static Boolean getCellBooleanData(int RowNum, int ColNum) throws Exception{
		Cell = ExcelWSheet.getRow(RowNum).getCell(ColNum);
		return Cell.getBooleanCellValue();
	}

	/**Get All the Data From a Particular Column ***/
	public static String [] getAllDataFromColumn(int ColNum) throws Exception{
		String values[]=new String[getNumberOfRows()-1];
		int index=0;
		for(int RowNum=1;RowNum<getNumberOfRows();RowNum++){
			Cell = ExcelWSheet.getRow(RowNum).getCell(ColNum);
			values[index++]=Cell.getStringCellValue();

		}
		//log.info("Size of Row values->>>> : "+values.length);
		return values;
	}


}
