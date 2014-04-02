package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelGenerator {

	private final ArrayList<Integer> columnsToHide = new ArrayList<Integer>() {
		{
			// add(1);
			add(3);
			add(4);
			add(8);
			add(9);
			add(10);
		}
	};
	private final CellStyle hiddenStyle;
	private SortedSet<String> sampleNames;
	private Map<String, String[]> baselineCsv;
	private Map<String, String[]> testlineCsv;

	ExcelGenerator(String baselineDirectory, String testlineDirectory) {
		XSSFWorkbook excelDocument = new XSSFWorkbook();
		DataFormat format = excelDocument.createDataFormat();
		hiddenStyle = excelDocument.createCellStyle();
		hiddenStyle.setHidden(true);
		hiddenStyle.setDataFormat(format.getFormat(";;;"));
		XSSFSheet excelSheet = excelDocument.createSheet("JMeter");
		for (Integer i : columnsToHide) {
			excelSheet.setColumnWidth(i, 0);
			excelSheet.setColumnHidden(i, true);
			excelSheet.setColumnWidth(i + 4 + 11, 0);
			excelSheet.setColumnHidden(i + 4 + 11, true);
		}
		parseSummaries(baselineDirectory, testlineDirectory);
		System.out.println("Creating New Excel Sheet");
		setupExcelHeader(excelSheet);
		{
			int rowNum = 0;
			Iterator<String> sampleIterator = sampleNames.iterator();
			while (sampleIterator.hasNext()) {
				rowNum++;
				Row currentRow = excelSheet.createRow(rowNum);
				String sampleName = sampleIterator.next();
				int differential = 0;
				addSamples(currentRow, sampleName, differential, baselineCsv);
				differential += 4 + 11;// (baselineCsv.get(sampleName) == null ?
										// 0 :
										// baselineCsv.get(sampleName).length);
				addSamples(currentRow, sampleName, differential, testlineCsv);
			}
		}

		saveExcelToFile(excelDocument);
	}

	private void addSamples(Row currentRow, String sampleName, int differential, Map<String, String[]> csvMap) {
		if (csvMap.get(sampleName) == null) {
			return;
		}
		int width = csvMap.get(sampleName).length;
		for (int i = 0; i < width; i++) {
			System.out.println(csvMap.get(sampleName)[i]);
			Cell currentCell = currentRow.createCell(i + differential);
			currentCell.setCellValue(csvMap.get(sampleName)[i]);
			if (columnsToHide.contains(i)) {
				currentCell.setCellStyle(hiddenStyle);
			}
		}
	}

	private void cleaningSampleNames() {
		System.out.println("Cleaning the CSVHEADER and TOTAL from sample names. We place these manually.");
		sampleNames.remove("CSVHEADER");
		sampleNames.remove("TOTAL");
	}

	private void setupExcelHeader(XSSFSheet excelSheet) {
		{
			// Creating header row
			Row row = excelSheet.createRow((short) 0);
			addSamples(row, "CSVHEADER", 0, baselineCsv);
			addSamples(row, "CSVHEADER", 4 + 11, testlineCsv);
		}
	}

	private void saveExcelToFile(XSSFWorkbook excelDocument) {
		try {
			File result = File.createTempFile("workbook", ".xlsx");
			FileOutputStream fileOut = new FileOutputStream(result);
			excelDocument.write(fileOut);
			fileOut.close();
			System.out.println("File dumped to " + result.getAbsolutePath());
			//Runtime.getRuntime().exec("xdg-open " + result.getAbsolutePath());
			System.out.println("Opening file with default viewer.");
			Desktop.getDesktop().open(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseSummaries(String baselineDirectory, String testlineDirectory) {
		baselineCsv = getSummaryMap(baselineDirectory);
		testlineCsv = getSummaryMap(testlineDirectory);
		sampleNames = new TreeSet<String>();
		{
			sampleNames.addAll(baselineCsv.keySet());
			sampleNames.addAll(testlineCsv.keySet());
		}
		cleaningSampleNames();
	}

	private Map<String, String[]> getSummaryMap(String directory) {
		String csvFile = iterateDirectory(directory, "_Summary.csv");
		Map<String, String[]> csvMap = new HashMap<String, String[]>();
		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].compareTo("sampler_label") == 0) {
					csvMap.put("CSVHEADER", values);
				} else {
					csvMap.put(values[0], values);
				}
			}
		} catch (IOException e) {
		}
		return csvMap;
	}

	private static String iterateDirectory(String directory, String fileSuffix) {
		String result = null;
		File folder = new File(directory);
		File[] filesInDirectory = folder.listFiles();
		for (File file : filesInDirectory) {
			String currentFileString = file.getAbsolutePath();
			if (currentFileString.endsWith(fileSuffix)) {
				result = currentFileString;
				break;
			}
		}
		return result;
	}
}