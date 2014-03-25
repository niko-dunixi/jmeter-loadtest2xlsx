package application;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import kg.apc.jmeter.DirectoryAnchor;

import org.apache.jmeter.util.JMeterUtils;

public class Application {

	private Application(String[] args) throws IOException, URISyntaxException {
		String fileDir = "/home/paulbaker/LoadTest/Results/JMeter/JMeter/";
		String file = "JmeterRawResults_20140324-0500_BASELINE.csv";
		JMeterHandler jmeter = new JMeterHandler(fileDir + file);
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		new Application(args);
	}
}
