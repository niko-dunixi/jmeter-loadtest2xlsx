package application;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.util.JMeterUtils;

import kg.apc.jmeter.DirectoryAnchor;
import kg.apc.jmeter.PluginsCMDWorker;

public class JMeterHandler {

	private static File jmeterProps;
	private static String tempDirectory;

	private final PluginsCMDWorker worker;

	public JMeterHandler(String filename) throws URISyntaxException, IOException {
		if (jmeterProps == null || tempDirectory == null) {
			createJMeterEnv();
		}

		worker = new PluginsCMDWorker();
		worker.setInputFile(filename);
		

		worker.setGraphWidth(650);
		worker.setGraphHeight(430);
		worker.setRelativeTimes(0);
		worker.setAutoScaleRows(1);
		worker.addExportMode(PluginsCMDWorker.EXPORT_PNG);

		worker.setPluginType("ResponseTimesOverTime");
		worker.setOutputPNGFile(tempDirectory + "test.png");
		int result = worker.doJob();
	}

	private static void createJMeterEnv() {
		File propsFile = null;
		try {
			propsFile = File.createTempFile("jmeter-plugins", ".properties");
			propsFile.deleteOnExit();
			JMeterUtils.loadJMeterProperties(propsFile.getAbsolutePath());
			JMeterUtils.setJMeterHome(new DirectoryAnchor().toString());
			JMeterUtils.setLocale(new Locale("ignoreResources"));
			
			tempDirectory = propsFile.getParent() + "/";
			jmeterProps = propsFile;
		} catch (IOException ex) {
			ex.printStackTrace(System.err);
		}
	}
}
