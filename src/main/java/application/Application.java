package application;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import kg.apc.jmeter.DirectoryAnchor;

import org.apache.jmeter.util.JMeterUtils;

public class Application {

	private Application(String[] args) throws IOException, URISyntaxException {
		JMeterHandler jmeter = new JMeterHandler(args[0]);
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		new Application(args);
	}
}
