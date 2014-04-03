package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Collections;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelGenerator {

	private final ArrayList<Integer> hiddenColumns = new ArrayList<Integer>() {
		{
			add(3);
			add(4);
			add(8);
			add(9);
			add(10);
		}
	};
	private final int percentageColumn = 7;
	private CellStyle hiddenStyle;
	private CellStyle percentStyle;
	private CellStyle titleStyle;

	// private SortedSet<String> sampleNames;
	// private Map<String, String[]> baselineCsv;
	// private Map<String, String[]> testlineCsv;

	ExcelGenerator(List<JMeterParsedResults> loadtestResults) {

		XSSFWorkbook excelDocument = new XSSFWorkbook();
		initStyles(excelDocument);
		XSSFSheet excelSheet = excelDocument.createSheet("JMeter");
		// for (Integer i : hiddenColumns) {
		// excelSheet.setColumnWidth(i, 0);
		// excelSheet.setColumnHidden(i, true);
		// excelSheet.setColumnWidth(i + 4 + 11, 0);
		// excelSheet.setColumnHidden(i + 4 + 11, true);
		// }

		int maxDifferential = loadtestResults.get(0).getCsvMap().get("CSVHEADER").length;
		SortedSet<String> sampleNames = new TreeSet<String>();
		for (JMeterParsedResults loadtest : loadtestResults) {
			sampleNames.addAll(loadtest.getCsvMap().keySet());
		}
		sampleNames.remove("CSVHEADER");
		sampleNames.remove("TOTAL");

		// {
		// int row = 0;
		// String sample = "CSVHEADER";
		// boolean compairisonColumns = false;
		// }
		doRowSample(loadtestResults, excelSheet, maxDifferential, 0, "CSVHEADER", false);
		int lastRow = doExcelBody(loadtestResults, excelSheet, maxDifferential, sampleNames);
		doRowSample(loadtestResults, excelSheet, maxDifferential, lastRow++, "CSVHEADER", false);
		doRowSample(loadtestResults, excelSheet, maxDifferential, lastRow++, "TOTAL", true);

		// excelSheet.autoSizeColumn(0);
		// excelSheet.autoSizeColumn(4 + 11);
		saveExcelToFile(excelDocument);
	}

	private void initStyles(XSSFWorkbook excelDocument) {
		DataFormat format = excelDocument.createDataFormat();
		hiddenStyle = excelDocument.createCellStyle();
		hiddenStyle.setHidden(true);
		hiddenStyle.setDataFormat(format.getFormat(";;;"));

		percentStyle = excelDocument.createCellStyle();
		percentStyle.setDataFormat(format.getFormat("0.000%"));

		XSSFFont bold = excelDocument.createFont();
		bold.setBold(true);
		bold.setFontHeight(10d);
		titleStyle = excelDocument.createCellStyle();
		titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
		//titleStyle.setShrinkToFit(true);
		titleStyle.setFont(bold);
	}

	private void doRowSample(List<JMeterParsedResults> loadtestResults, XSSFSheet excelSheet, int maxDifferential, int row, String sample, boolean compairisonColumns) {
		int currentDifferential = 0;
		Row currentRow = excelSheet.createRow(row);// excelSheet.getRow(row);
		for (int i = 0; i < loadtestResults.size(); i++) {
			JMeterParsedResults currentResults = loadtestResults.get(i);
			String[] summaryData = currentResults.getCsvMap().get(sample);
			for (int j = 0; j < summaryData.length; j++) {
				Cell currentCell = currentRow.createCell(currentDifferential + j);
				try {
					currentCell.setCellValue(Double.parseDouble(summaryData[j]));
				} catch (NumberFormatException e) {
					currentCell.setCellValue(summaryData[j]);
				}
				if (!compairisonColumns){
					currentCell.setCellStyle(titleStyle);
				}
				if (i == percentageColumn) {
					currentCell.setCellStyle(percentStyle);
				}
			}
			currentDifferential += maxDifferential;
			// System.out.println(currentDifferential);
			if (compairisonColumns && i + 1 < loadtestResults.size()) {
				// Average Response Times Comparison Column
				Cell avgCell = currentRow.createCell(currentDifferential + 1);
				String avgCellFormula = CellReference.convertNumToColString(currentDifferential + 6) + (row + 1) + " / "
						+ CellReference.convertNumToColString(currentDifferential - 9) + (row + 1);
				avgCell.setCellFormula(avgCellFormula);
				avgCell.setCellStyle(percentStyle);
				// Error Comparison Column
				Cell errCell = currentRow.createCell(currentDifferential + 2);
				String errCellFormula = CellReference.convertNumToColString(currentDifferential + 11) + (row + 1) + " - "
						+ CellReference.convertNumToColString(currentDifferential - 4) + (row + 1);
				errCell.setCellFormula(errCellFormula);
				errCell.setCellStyle(percentStyle);
			} else if (!compairisonColumns && i + 1 < loadtestResults.size()) {
				Cell avgCell = currentRow.createCell(currentDifferential + 1);
				avgCell.setCellValue("Avg / Avg");
				avgCell.setCellStyle(titleStyle);
				Cell errCell = currentRow.createCell(currentDifferential + 2);
				errCell.setCellValue("Δ Error");
				errCell.setCellStyle(titleStyle);
			}
			currentDifferential += 4;
		}
	}

	private int doExcelBody(List<JMeterParsedResults> loadtestResults, XSSFSheet excelSheet, int maxDifferential, SortedSet<String> sampleNames) {
		int currentRowIndex = 1;
		Iterator<String> sampleNameIterator = sampleNames.iterator();
		while (sampleNameIterator.hasNext()) {
			String currentSampleName = sampleNameIterator.next();
			doRowSample(loadtestResults, excelSheet, maxDifferential, currentRowIndex, currentSampleName, true);
			currentRowIndex++;
		}
		return currentRowIndex;
	}

	// private void setupExcelFooter(XSSFSheet excelSheet, int rowNum) {
	// rowNum++;
	// Row row = excelSheet.createRow((short) rowNum);
	// addSamples(row, "TOTAL", 0, baselineCsv);
	// processBodyCompairison(rowNum, row, "TOTAL", baselineCsv, testlineCsv);
	// addSamples(row, "TOTAL", 4 + 11, testlineCsv);
	// excelSheet.autoSizeColumn(0);
	// excelSheet.autoSizeColumn(4 + 11);
	// }
	//
	// private void processBodyCompairison(int rowNum, Row currentRow, String
	// sampleName, Map<String, String[]> csvMapOne, Map<String, String[]>
	// csvMapTwo) {
	// if (csvMapOne.get(sampleName) != null && csvMapTwo.get(sampleName) !=
	// null) {
	// int rowAsAnInt = rowNum + 1;
	// String avgFormula = "R" + rowAsAnInt + "/C" + rowAsAnInt;
	// String errFormula = "W" + rowAsAnInt + "-H" + rowAsAnInt;
	// Cell avgCell = currentRow.createCell(12, XSSFCell.CELL_TYPE_FORMULA);
	// avgCell.setCellFormula(avgFormula);
	// avgCell.setCellStyle(percentStyle);
	// Cell errCell = currentRow.createCell(13, XSSFCell.CELL_TYPE_FORMULA);
	// errCell.setCellFormula(errFormula);
	// errCell.setCellStyle(percentStyle);
	// }
	// }
	//
	// private void addSamples(Row currentRow, String sampleName, int
	// differential, Map<String, String[]> csvMap) {
	// if (csvMap.get(sampleName) == null) {
	// return;
	// }
	// int width = csvMap.get(sampleName).length;
	// for (int i = 0; i < width; i++) {
	// Cell currentCell = currentRow.createCell(i + differential);
	// try {
	// currentCell.setCellValue(Double.parseDouble(csvMap.get(sampleName)[i]));
	// } catch (NumberFormatException e) {
	// System.out.print("String: ");
	// currentCell.setCellValue(csvMap.get(sampleName)[i]);
	// }
	// if (hiddenColumns.contains(i)) {
	// currentCell.setCellStyle(hiddenStyle);
	// }
	// if (percentageColumn == i) {
	// currentCell.setCellStyle(percentStyle);
	// }
	// System.out.println(csvMap.get(sampleName)[i]);
	// }
	// }
	//
	// private void cleaningSampleNames() {
	// System.out.println("Cleaning the CSVHEADER and TOTAL from sample names. We place these manually.");
	// sampleNames.remove("CSVHEADER");
	// sampleNames.remove("TOTAL");
	// }
	//
	// private void setupExcelHeader(XSSFSheet excelSheet) {
	// // Creating header row
	// Row row = excelSheet.createRow((short) 0);
	// addSamples(row, "CSVHEADER", 0, baselineCsv);
	// addSamples(row, "CSVHEADER", 4 + 11, testlineCsv);
	// }
	//
	private void saveExcelToFile(XSSFWorkbook excelDocument) {
		try {
			File result = File.createTempFile("workbook", ".xlsx");
			FileOutputStream fileOut = new FileOutputStream(result);
			excelDocument.write(fileOut);
			fileOut.close();
			System.out.println("File dumped to " + result.getAbsolutePath());
			System.out.println("Opening file with default viewer.");
			Desktop.getDesktop().open(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//
	// private void parseSummaries(String baselineDirectory, String
	// testlineDirectory) {
	// baselineCsv = getSummaryMap(baselineDirectory);
	// testlineCsv = getSummaryMap(testlineDirectory);
	// sampleNames = new TreeSet<String>();
	// {
	// sampleNames.addAll(baselineCsv.keySet());
	// sampleNames.addAll(testlineCsv.keySet());
	// }
	// cleaningSampleNames();
	// }
	//
	// private Map<String, String[]> getSummaryMap(String directory) {
	// String csvFile = iterateDirectory(directory, "_Summary.csv");
	// Map<String, String[]> csvMap = new HashMap<String, String[]>();
	// try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
	// String line;
	// while ((line = br.readLine()) != null) {
	// String[] values = line.split(",");
	// if (values[0].compareTo("sampler_label") == 0) {
	// csvMap.put("CSVHEADER", values);
	// } else {
	// csvMap.put(values[0], values);
	// }
	// }
	// } catch (IOException e) {
	// }
	// return csvMap;
	// }
	//
	// private static String iterateDirectory(String directory, String
	// fileSuffix) {
	// String result = null;
	// File folder = new File(directory);
	// File[] filesInDirectory = folder.listFiles();
	// for (File file : filesInDirectory) {
	// String currentFileString = file.getAbsolutePath();
	// if (currentFileString.endsWith(fileSuffix)) {
	// result = currentFileString;
	// break;
	// }
	// }
	// return result;
	// }
}