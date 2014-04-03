package application;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelGenerator {

	private final ArrayList<Integer> hiddenColumns = new ArrayList<Integer>() {
		private static final long serialVersionUID = -660313218075841778L;
		{
			add(3);
			add(4);
			add(8);
			add(9);
			add(10);
		}
	};
	private final ArrayList<Integer> percentageColumn = new ArrayList<Integer>() {
		private static final long serialVersionUID = 3075748202378866049L;
		{
			add(7);
			add(8);
			add(9);
		}
	};
	private XSSFCellStyle hiddenStyle;
	private XSSFCellStyle percentStyle;
	private XSSFCellStyle titleStyle;
	private XSSFCellStyle failStyle;
	private XSSFCellStyle warnStyle;

	ExcelGenerator(List<JMeterParsedResults> loadtestResults) {

		XSSFWorkbook excelDocument = new XSSFWorkbook();
		initStyles(excelDocument);
		XSSFSheet excelSheet = excelDocument.createSheet("JMeter");

		int maxDifferential = loadtestResults.get(0).getCsvMap().get("CSVHEADER").length;
		SortedSet<String> sampleNames = initSampleNames(loadtestResults);
		doRowSample(loadtestResults, excelSheet, maxDifferential, 0, "CSVHEADER", false);
		int lastRow = doExcelBody(loadtestResults, excelSheet, maxDifferential, sampleNames);
		doRowSample(loadtestResults, excelSheet, maxDifferential, lastRow++, "CSVHEADER", false);
		doRowSample(loadtestResults, excelSheet, maxDifferential, lastRow++, "TOTAL", true);
		doFormatColumns(loadtestResults, excelSheet, maxDifferential);
		lastRow += 4;
		CreationHelper creationHelper = excelDocument.getCreationHelper();
		Drawing drawing = excelSheet.createDrawingPatriarch();
		// for(JMeterParsedResults loadtest : loadtestResults){
		for (int i = 0; i < loadtestResults.size(); i++) {
			List<byte[]> pictureArray = loadtestResults.get(i).getImages();
			for (int j = 0; j < pictureArray.size(); j++) {
				ClientAnchor anchor = creationHelper.createClientAnchor();
				//anchor.setCol1((maxDifferential * i) + (4 * i) + maxDifferential);
				//anchor.setRow1(lastRow + j * 10);
				anchor.setCol1(i * maxDifferential + i * 4);
				anchor.setCol2(anchor.getCol1() + maxDifferential);
			    anchor.setRow1(lastRow + j * 16);
			    anchor.setRow2(anchor.getRow1() + 16);
			    
				Picture pict = drawing.createPicture(anchor, excelDocument.addPicture(pictureArray.get(j), XSSFWorkbook.PICTURE_TYPE_PNG));
				// pict.resize();
			}
		}

		saveExcelToFile(excelDocument);
	}

	private SortedSet<String> initSampleNames(List<JMeterParsedResults> loadtestResults) {
		SortedSet<String> sampleNames = new TreeSet<String>();
		for (JMeterParsedResults loadtest : loadtestResults) {
			sampleNames.addAll(loadtest.getCsvMap().keySet());
			loadtest.getCsvMap().get("CSVHEADER")[0] = loadtest.getTestName();
		}
		sampleNames.remove("CSVHEADER");
		sampleNames.remove("TOTAL");
		return sampleNames;
	}

	private void doFormatColumns(List<JMeterParsedResults> loadtestResults, XSSFSheet excelSheet, int maxDifferential) {
		for (int i = 0; i < loadtestResults.size(); i++) {
			for (Integer j : hiddenColumns) {
				excelSheet.setColumnWidth(j + (4 * i) + (maxDifferential * i), 0);
				excelSheet.setColumnHidden(j + (4 * i) + (maxDifferential * i), true);
			}
			excelSheet.setColumnWidth(i * maxDifferential + i * 4, 6034); // 3.34"
		}
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
		// titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
		titleStyle.setAlignment(CellStyle.ALIGN_JUSTIFY);
		titleStyle.setFont(bold);

		XSSFFont failFont = excelDocument.createFont();
		failFont.setColor(new XSSFColor(new Color(255, 0, 0)));
		failStyle = excelDocument.createCellStyle();
		failStyle.setFont(failFont);

		XSSFFont warnFont = excelDocument.createFont();
		warnFont.setColor(new XSSFColor(new Color(255, 255, 0)));
		warnStyle = excelDocument.createCellStyle();
		warnStyle.setFont(warnFont);
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
				if (!compairisonColumns) {
					currentCell.setCellStyle(titleStyle);
				}
				if (compairisonColumns && percentageColumn.contains(j)) {
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
}