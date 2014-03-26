package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.util.JMeterUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import kg.apc.jmeter.DirectoryAnchor;
import kg.apc.jmeter.PluginsCMDWorker;

public class JMeterHandler {

	private static String tempDirectory;

	private final PluginsCMDWorker worker;

	public JMeterHandler(String filename) throws URISyntaxException, IOException {
		// if (jmeterProps == null || tempDirectory == null) {
		// // createJMeterEnv();
		//
		// }
		setupJMeter();

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

	private static void setupJMeter() {
		final String[] props = { "jmeter", "saveservice", "system", "upgrade", "user" };
		final String suffix = ".properties";
		for (String prop : props) {
			final URL resourceUrl = Resources.getResource(prop + suffix);
			try {
				final File tempPropsFile = File.createTempFile(prop, suffix);
				tempPropsFile.deleteOnExit();
				BufferedWriter bw = new BufferedWriter(new FileWriter(tempPropsFile));
				bw.write(Resources.toString(resourceUrl, Charsets.UTF_8));
				bw.close();
				if (tempDirectory == null){
					tempDirectory = tempPropsFile.getParent() + "/";
				}
				if (prop.compareTo("jmeter") == 0) {
					JMeterUtils.loadJMeterProperties(tempPropsFile.getAbsolutePath());
				} else {
					JMeterUtils.loadProperties(tempPropsFile.getAbsolutePath());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		JMeterUtils.setJMeterHome(new DirectoryAnchor().toString());
		JMeterUtils.setLocale(new Locale("ignoreResources"));
	}

//	private static void createJMeterEnv() {
//		try {
//			final File propsFile = File.createTempFile("jmeter-plugins", ".properties");
//			propsFile.deleteOnExit();
//			JMeterUtils.loadJMeterProperties(propsFile.getAbsolutePath());
//			JMeterUtils.setJMeterHome(new DirectoryAnchor().toString());
//			JMeterUtils.setLocale(new Locale("ignoreResources"));
//
//			tempDirectory = propsFile.getParent() + "/";
//			jmeterProps = propsFile;
//		} catch (IOException ex) {
//			ex.printStackTrace(System.err);
//		}
//	}
}
