package com.openxsl.config.dal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.util.StringUtils;

public class ExcelUtils {
	public static final String EXCEL_XLS = ".xls";
    public static final String EXCEL_XLSX = ".xlsx";
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);
    
    /**
     * 读取指定Sheet页的内容
     * @param filepath 文件全路径
     * @param skipFirst 跳过首行
     * @param sheetNo sheet序号,从0开始,如果读取全文sheetNo设置null
     */
    public static List<Map<String,?>> readFile(String filepath, boolean skipFirst,
						Integer... sheetNo) throws InvalidFormatException, IOException {
		Workbook workbook = getWorkbook(filepath);
		List<Map<String,?>> data = new ArrayList<Map<String,?>>();
		if (sheetNo.length == 0) {
			sheetNo = new Integer[] {0};
		}
		for (Integer n : sheetNo) {
			Sheet sheet = getSheet(workbook, n);
			if (sheet != null) {
				data.addAll( readSheet(sheet,skipFirst) );
			}
		}
		return data;
	}
    
    public static List<Map<String,?>> readFile(InputStream is, boolean skipFirst, Integer... sheetNo)
    				throws InvalidFormatException, IOException{
    	Workbook workbook = WorkbookFactory.create(is);
    	List<Map<String,?>> data = new ArrayList<Map<String,?>>();
        for (Integer n : sheetNo) {
        	Sheet sheet = getSheet(workbook, n);
        	if (sheet != null) {
        		data.addAll( readSheet(sheet,skipFirst) );
        	}
        }
        return data;
    }
    
    /**
     * 将数据写入Workbook
     * @param data 页数据
     * @param headers  标题行
     * @param workbook Workbook对象
     * @param sheetNo  
     * @param consumer 读取行数据的函数
     */
    public static <T> void writeWorkbook(List<T> data, String[] headers,
    						Workbook workbook, int sheetNo,
    						Function<T, Object[]> converter) {
    	Sheet sheet = workbook.createSheet("Sheet"+sheetNo);
        int rowNum = 0;
        //添加标题行
        if (headers != null) {
        	Row row = sheet.createRow(0);
	        for(int i=0; i<headers.length; i++){
	            Cell cell = row.createCell(i);
	            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
	            cell.setCellValue(text);
	        }
	        rowNum++;
        }
        
        for (T model : data) {
            Row row = sheet.createRow(rowNum++);
            int col = 0;
            for (Object value : converter.apply(model)) {
            	setCellValue(row.createCell(col++), value);
            }
        }
    }
    
    public static <T> void exportData(OutputStream os, List<T> data, String[] headers,
							Function<T, Object[]> consumer) {
    	HSSFWorkbook workbook = new HSSFWorkbook();
    	writeWorkbook(data, headers, workbook, 1, consumer);

        try {
            workbook.write(os);
        } catch (IOException e) {
            logger.error("export data error: ", e);
        } finally {
        	try {
				workbook.close();
			} catch (IOException e) {
			}
        }
    }
    
    public static <T> void exportRemoteFile(String fileName, List<T> data, String[] headers,
	    					Function<T, Object[]> consumer,
	    					HttpServletResponse response) {
        try {
            String name = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
            response.setHeader("Content-disposition", "attachment;filename=" + name);
            response.setContentType("application/octet-stream");
            response.flushBuffer();
            
            exportData(response.getOutputStream(), data, headers, consumer);
        } catch (IOException e) {
            logger.error("export data error: ", e);
        } 
    }
    
    /**
     * 读取指定Sheet的一行数据
     * @param filepath filepath 文件全路径
     * @param sheetNo sheet序号,从0开始,如果读取全文sheetNo设置null
     */
    public static Map<String,Object> readRowData(String filepath, int sheetNo, int rowNo)
    				throws InvalidFormatException, IOException{
    	Workbook workbook = getWorkbook(filepath);
    	Sheet sheet = getSheet(workbook, sheetNo);
    	if (sheet == null) {
    		return new QueryMap<Object>();
    	} else {
    		return readRow(sheet, rowNo);
    	}
    }
    
    static Workbook getWorkbook(String filepath) throws InvalidFormatException, IOException {
        if (StringUtils.isEmpty(filepath)) {
            throw new IllegalArgumentException("文件路径不能为空");
        } else {
        	int dotPos = filepath.lastIndexOf(".");
            String suffix = dotPos<1 ? "" : filepath.substring(dotPos);
            if (StringUtils.isEmpty(suffix)) {
                throw new IllegalArgumentException("文件后缀不能为空");
            }
            if (EXCEL_XLS.equals(suffix) || EXCEL_XLSX.equals(suffix)) {
                try (InputStream is = new FileInputStream(filepath);){
                	return WorkbookFactory.create(is);
                }
            } else {
                throw new IllegalArgumentException("该文件非Excel文件");
            }
        }
    }
    
    private static Sheet getSheet(Workbook workbook, int sheetNo) {
        if (workbook == null) {
        	return null;
        }
        int numberOfSheets = workbook.getNumberOfSheets();
        if (sheetNo < 0 || sheetNo >= numberOfSheets) {
    		return null;
    	} else {
    		return workbook.getSheetAt(sheetNo);
    	}
    }
    
    private static QueryMap<Object> readRow(Sheet sheet, int rowNo){
    	Row row = sheet.getRow(rowNo);
		if (row != null){
    		int columNos = row.getLastCellNum();
            QueryMap<Object> rowMap = new QueryMap<Object>(columNos);
            for (int j = 0; j <= columNos; j++) {
            	rowMap.put(getColumnName(j), getCellValue(row.getCell(j), ""));
            }
            return rowMap;
		}
		return new QueryMap<Object>();
    }
    
    private static List<QueryMap<Object>> readSheet(Sheet sheet, boolean skipFirst) {
        int rowNos = sheet.getLastRowNum();
        List<QueryMap<Object>> rowData = new ArrayList<QueryMap<Object>>(rowNos+1);
        for (int i = (skipFirst ? 1 : 0); i <= rowNos; i++) {
        	rowData.add( readRow(sheet, i) );
        }
        return rowData;
    }
    
    private static String getColumnName(int i) {
    	if (i < 26) {
    		char ch = (char)('A' + i);
    		return Character.toString(ch);
    	} else {
    		char ch1 = (char)('A' + (i/26-1));
    		char ch2 =  (char)('A' + i%26);
    		return Character.toString(ch1) + Character.toString(ch2);
    	}
    }
    private static Object getCellValue(Cell cell, String dateFormat) {
        if (cell == null || cell.toString().trim().equals("")) {
            return null;
        }
        final CellType CELL_TYPE = cell.getCellTypeEnum();
        if (CELL_TYPE == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {// 判断是否为日期类型  
//            	cellValue = new DataFormatter().formatRawCellContents(dbl, 0, dateFormat);
            	return cell.getDateCellValue();
            } else {
                return cell.getNumericCellValue();
            }
        } else if (CELL_TYPE == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (CELL_TYPE == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (CELL_TYPE == CellType.FORMULA) {
        	return cell.getCellFormula() + "";
        } else if (CELL_TYPE == CellType.BLANK) {
        	return "";
        } else /*if (CELL_TYPE == CellType.ERROR)*/ {
        	return "非法字符";
        }
    }
    
    private static void setCellValue(Cell cell, Object value) {
    	if (value != null) {
    		if (value instanceof String) {
        		cell.setCellType(CellType.STRING);
        		cell.setCellValue((String)value);
        	} else if (value instanceof Number) {
        		cell.setCellType(CellType.NUMERIC);
        		cell.setCellValue(new Double(value.toString()));
        	} else if (value instanceof Boolean) {
        		cell.setCellType(CellType.BOOLEAN);
        	} else if (value instanceof java.util.Date) {
        		cell.setCellType(CellType.NUMERIC);
        		cell.setCellValue((java.util.Date)value);
        	}
        }
    }

}
