package application;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {

//	private Application(String baselineFilePath, String testFilePath) {
//		try {
//			JMeterHandler jmeter = new JMeterHandler();
//			String baslineFolder = jmeter.parseRawFile(baselineFilePath);
//			String resultsFolder = jmeter.parseRawFile(testFilePath);
//			new ExcelGenerator(baslineFolder, resultsFolder);
//
//		} catch (JMeterHandlerSetupException | JMeterHandlerParseException e) {
//			e.printStackTrace();
//		}
//	}
	
	private Application(Set<String> filenames){
		Map<String, JMeterHandler> jmeterParsers = new HashMap<String, JMeterHandler>();
	}

	public static void main(String[] args) throws YourArgumentIsInvalid {
		Set<String> filenames = new TreeSet<String>();
		for(String filename : args){
			filenames.add(filename);
		}
		if (filenames.size() <= 1) {
			throw new YourArgumentIsInvalid();
		}
		new Application(filenames);
	}
}