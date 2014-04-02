package application;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {

	private Application(String baselineFilePath, String testFilePath) {
		try {
			JMeterHandler jmeter = new JMeterHandler();
			String baslineFolder = jmeter.parseRawFile(baselineFilePath);
			String resultsFolder = jmeter.parseRawFile(testFilePath);
			new ExcelGenerator(baslineFolder, resultsFolder);
			
		} catch (JMeterHandlerSetupException | JMeterHandlerParseException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
	}
}