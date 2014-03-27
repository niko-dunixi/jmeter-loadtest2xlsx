package application;

import java.io.IOException;

public class Application {

	private Application(String[] args) {
		try {
			JMeterHandler jmeter = new JMeterHandler();
			// only here for debugging purposes. This is obviously not
			// permanent.
			Runtime.getRuntime().exec("nautilus " + jmeter.getTempDir());
			jmeter.parseSummary(args[0]);
			jmeter.parseGraphs(args[0]);
		} catch (JMeterHandlerSetupException | JMeterHandlerParseException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		new Application(args);
	}
}