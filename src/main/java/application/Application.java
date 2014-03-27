package application;

import java.io.IOException;

public class Application {

	private Application(String[] args) {
		try {
			JMeterHandler jmeter = new JMeterHandler();
			Runtime.getRuntime().exec("nautilus "+ jmeter.getTempDir());
			jmeter.parseRawFile(args[0]);
		} catch (JMeterHandlerSetupException | JMeterHandlerParseException | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		
		new Application(args);
	}
}