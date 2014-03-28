package application;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Application {

	private Application(String baselineFilePath, String testFilePath) {
		try {
			JMeterHandler jmeter = new JMeterHandler();
			// only here for debugging purposes. This is obviously not
			// permanent.
			Runtime.getRuntime().exec("nautilus " + jmeter.getTempDir());
			jmeter.parseSummary(baselineFilePath);
			jmeter.parseGraphs(baselineFilePath);
			jmeter.parseSummary(testFilePath);
			jmeter.parseGraphs(testFilePath);
		} catch (JMeterHandlerSetupException | JMeterHandlerParseException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		// for (String arg : args) {
		// System.out.println(arg);
		// }
		// CLI validation
		String[] files = null;
		if ((files = validateArguments(args)) != null) {
			new Application(files[0], files[1]);
		} else {
			printUsage();
		}
	}

	public static String[] validateArguments(String[] args) {
		try {
			String baselineFilePath = null;
			String testFilePath = null;
			for (String argument : args) {
				char argChar = 'h';
				if (argument.length() > 2) {
					argChar = argument.charAt(1);
				}
				switch (argChar) {
				case 'b':
					if (baselineFilePath != null) {
						throw new YourArgumentIsInvalid();
					}
					baselineFilePath = parseArgumentRegex(argument);
					break;
				case 't':
					if (testFilePath != null) {
						throw new YourArgumentIsInvalid();
					}
					testFilePath = parseArgumentRegex(argument);
					break;
				case 'h':
				default:
					throw new YourArgumentIsInvalid();
				}
			}
			if (baselineFilePath == null || testFilePath == null) {
				throw new YourArgumentIsInvalid();
			}
			return new String[] { baselineFilePath, testFilePath };
		} catch (YourArgumentIsInvalid e) {
			return null;
		}
	}

	public static String parseArgumentRegex(String argument) throws YourArgumentIsInvalid {
		String result = "";
		Pattern pattern = Pattern.compile("^-[a-z]{1}=(.+)$", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(argument);
		if (matcher.find()) {
			result = matcher.group(1);
		} else {
			throw new YourArgumentIsInvalid();
		}
		return result;
	}

	private static void printUsage() {
		System.out.println("You're doing it wrong");
	}
}