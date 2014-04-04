package application;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Iterator;

import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.FontFormatting;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PatternFormatting;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
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
	private XSSFCellStyle fillerStyle;
	private SheetConditionalFormatting excelCondForm;
	private ConditionalFormattingRule failRule;
	private ConditionalFormattingRule warnRule;

	ExcelGenerator(List<JMeterParsedResults> loadtestResults) {

		XSSFWorkbook excelDocument = new XSSFWorkbook();
		XSSFSheet excelSheet = excelDocument.createSheet("JMeter");
		initStyles(excelDocument);
		initFont(excelDocument);
		initCondForm(excelSheet);

		int maxDifferential = loadtestResults.get(0).getCsvMap().get("CSVHEADER").length;
		SortedSet<String> sampleNames = initSampleNames(loadtestResults);
		doRowSample(loadtestResults, excelSheet, maxDifferential, 0, "CSVHEADER", false);
		int lastRow = doExcelBody(loadtestResults, excelSheet, maxDifferential, sampleNames);
		doRowSample(loadtestResults, excelSheet, maxDifferential, lastRow++, "CSVHEADER", false);
		doRowSample(loadtestResults, excelSheet, maxDifferential, lastRow++, "TOTAL", true);
		doFormatColumns(loadtestResults, excelSheet, maxDifferential);
		lastRow += 4;
		doGraphs(loadtestResults, excelDocument, excelSheet, maxDifferential, lastRow);

		saveExcelToFile(excelDocument);
	}

	public void doGraphs(List<JMeterParsedResults> loadtestResults, XSSFWorkbook excelDocument, XSSFSheet excelSheet, int maxDifferential, int lastRow) {
		CreationHelper creationHelper = excelDocument.getCreationHelper();
		Drawing drawing = excelSheet.createDrawingPatriarch();
		for (int i = 0; i < loadtestResults.size(); i++) {
			Map<String, byte[]> pictureMap = loadtestResults.get(i).getImages();
			Set<String> pictureNames = pictureMap.keySet();
			int j = 0;
			for (Iterator<String> pictureNameIterator = pictureNames.iterator(); pictureNameIterator.hasNext(); j++) {
				String pictureName = pictureNameIterator.next();
				ClientAnchor anchor = creationHelper.createClientAnchor();
				final int width = maxDifferential + 2;
				anchor.setCol1(i * maxDifferential + i * 4);
				anchor.setCol2(anchor.getCol1() + width);
				final int height = 20;
				anchor.setRow1(lastRow + j * height + 1);
				anchor.setRow2(anchor.getRow1() + height - 1);
				Row imgTitleRow = excelSheet.getRow(anchor.getRow1() - 1);
				if (imgTitleRow == null) {
					imgTitleRow = excelSheet.createRow(anchor.getRow1() - 1);
				}
				Cell imgTitleCell = imgTitleRow.createCell(anchor.getCol1());
				imgTitleCell.setCellValue(pictureName);
				/*Picture picture = */drawing.createPicture(anchor, excelDocument.addPicture(pictureMap.get(pictureName), XSSFWorkbook.PICTURE_TYPE_PNG));
			}
		}
	}

	private SortedSet<String> initSampleNames(List<JMeterParsedResults> loadtestResults) {
		SortedSet<String> sampleNames = new TreeSet<String>();
		for (JMeterParsedResults loadtest : loadtestResults) {
			sampleNames.addAll(loadtest.getCsvMap().keySet());
		}
		sampleNames.remove("CSVHEADER");
		sampleNames.remove("TOTAL");
		return sampleNames;
	}

	private void doFormatColumns(List<JMeterParsedResults> loadtestResults, XSSFSheet excelSheet, int maxDifferential) {
		final int sampleWidth = 6225;
		final int fillerWidth = 256;
		for (int i = 0; i < loadtestResults.size(); i++) {
			for (Integer j : hiddenColumns) {
				excelSheet.setColumnWidth(j + (4 * i) + (maxDifferential * i), 0);
				excelSheet.setColumnHidden(j + (4 * i) + (maxDifferential * i), true);
			}
			excelSheet.setColumnWidth(i * maxDifferential + i * 4, sampleWidth);
			if (i + 1 < loadtestResults.size()) {
				excelSheet.setColumnWidth(i * 4 + i * maxDifferential + maxDifferential, fillerWidth);
				excelSheet.setColumnWidth(i * 4 + i * maxDifferential + maxDifferential + 3, fillerWidth);
			}
		}
	}

	private void initStyles(XSSFWorkbook excelDocument) {
		DataFormat format = excelDocument.createDataFormat();
		hiddenStyle = excelDocument.createCellStyle();
		hiddenStyle.setHidden(true);
		hiddenStyle.setDataFormat(format.getFormat(";;;"));

		percentStyle = excelDocument.createCellStyle();
		percentStyle.setDataFormat(format.getFormat("0.000%"));

		fillerStyle = excelDocument.createCellStyle();
		fillerStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
		fillerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
	}

	public void initFont(XSSFWorkbook excelDocument) {
		XSSFFont bold = excelDocument.createFont();
		bold.setBold(true);
		bold.setFontHeight(10d);
		titleStyle = excelDocument.createCellStyle();
		titleStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		titleStyle.setAlignment(CellStyle.ALIGN_CENTER);
		titleStyle.setFont(bold);
	}

	public void initCondForm(XSSFSheet excelSheet) {
		excelCondForm = excelSheet.getSheetConditionalFormatting();
		failRule = excelCondForm.createConditionalFormattingRule(ComparisonOperator.GT, "119%");
		FontFormatting failFont = failRule.createFontFormatting();
		failFont.setFontStyle(false, true);
		failFont.setFontColorIndex(IndexedColors.RED.getIndex());
		PatternFormatting failPattern = failRule.createPatternFormatting();
		failPattern.setFillBackgroundColor(IndexedColors.BLACK.getIndex());

		warnRule = excelCondForm.createConditionalFormattingRule(ComparisonOperator.GT, "109%");
		FontFormatting warnFont = warnRule.createFontFormatting();
		warnFont.setFontStyle(false, true);
		warnFont.setFontColorIndex(IndexedColors.YELLOW.getIndex());
		PatternFormatting warnPattern = warnRule.createPatternFormatting();
		warnPattern.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
	}

	private void doRowSample(List<JMeterParsedResults> loadtestResults, XSSFSheet excelSheet, int maxDifferential, int row, String sample, boolean comparableRow) {
		int currentDifferential = 0;
		Row currentRow = excelSheet.createRow(row);
		for (int i = 0; i < loadtestResults.size(); i++) {
			doRowSampleTestStats(loadtestResults, sample, comparableRow, currentDifferential, currentRow, i);
			currentDifferential += maxDifferential;
			doRowSampleCompairColumns(loadtestResults, row, comparableRow, currentDifferential, currentRow, i);
			currentDifferential += 4;
		}
		if (!comparableRow) {
			currentRow.setHeight((short) 400);
		}
	}

	public void doRowSampleTestStats(List<JMeterParsedResults> loadtestResults, String sample, boolean comparableRow, int currentDifferential, Row currentRow, int i) {
		JMeterParsedResults currentResults = loadtestResults.get(i);
		if (currentResults.getCsvMap().containsKey(sample)) {
			String[] summaryData = currentResults.getCsvMap().get(sample);
			for (int j = 0; j < summaryData.length; j++) {
				Cell currentCell = currentRow.createCell(currentDifferential + j);
				try {
					currentCell.setCellValue(Double.parseDouble(summaryData[j]));
				} catch (NumberFormatException e) {
					currentCell.setCellValue(summaryData[j]);
				}
				if (!comparableRow) {
					currentCell.setCellStyle(titleStyle);
				}
				if (comparableRow && percentageColumn.contains(j)) {
					currentCell.setCellStyle(percentStyle);
				}
			}
		}
	}

	public void doRowSampleCompairColumns(List<JMeterParsedResults> loadtestResults, int row, boolean comparableRow, int currentDifferential, Row currentRow, int i) {
		if (comparableRow && i + 1 < loadtestResults.size()) {
			doAverageCell(row, currentDifferential, currentRow);
			doErrorCell(row, currentDifferential, currentRow);
			doFillerCell(currentRow, currentDifferential + 0);
			doFillerCell(currentRow, currentDifferential + 3);
		} else if (!comparableRow && i + 1 < loadtestResults.size()) {
			Cell avgCell = currentRow.createCell(currentDifferential + 1);
			avgCell.setCellValue("Avg / Avg");
			avgCell.setCellStyle(titleStyle);
			Cell errCell = currentRow.createCell(currentDifferential + 2);
			errCell.setCellValue("Δ Error");
			errCell.setCellStyle(titleStyle);
		}
	}

	public void doFillerCell(Row currentRow, int columnIndex) {
		Cell fillerCell = currentRow.createCell(columnIndex);
		fillerCell.setCellStyle(fillerStyle);
	}

	public void doAverageCell(int row, int currentDifferential, Row currentRow) {
		Cell avgCell = currentRow.createCell(currentDifferential + 1);
		{
			CellRangeAddress[] singleCellRange = new CellRangeAddress[1];
			singleCellRange[0] = new CellRangeAddress(avgCell.getRowIndex(), avgCell.getRowIndex(), avgCell.getColumnIndex(), avgCell.getColumnIndex());
			excelCondForm.addConditionalFormatting(singleCellRange, failRule, warnRule);
		}
		String avgCellFormula = CellReference.convertNumToColString(currentDifferential + 6) + (row + 1) + " / " + CellReference.convertNumToColString(currentDifferential - 9)
				+ (row + 1);
		avgCell.setCellFormula(avgCellFormula);
		avgCell.setCellStyle(percentStyle);
	}

	public void doErrorCell(int row, int currentDifferential, Row currentRow) {
		Cell errCell = currentRow.createCell(currentDifferential + 2);
		String errCellFormula = CellReference.convertNumToColString(currentDifferential + 11) + (row + 1) + " - " + CellReference.convertNumToColString(currentDifferential - 4)
				+ (row + 1);
		errCell.setCellFormula(errCellFormula);
		errCell.setCellStyle(percentStyle);
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