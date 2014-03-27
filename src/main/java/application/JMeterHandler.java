package application;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jmeter.util.JMeterUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import kg.apc.jmeter.PluginsCMDWorker;

public class JMeterHandler {

	private String tempDirectory;

	public JMeterHandler() throws JMeterHandlerSetupException {
		setupJMeter();
	}

	private void setTempDir(String tempPath) {
		tempDirectory = tempPath;
	}

	public String getTempDir() {
		return new String(tempDirectory);
	}

	public void parseRawFile(String filename) throws JMeterHandlerParseException {
		final String[] graphModes = { "TransactionsPerSecond", "ResponseTimesOverTime" };
		final String resultNameBase = setupFileName(filename);

		final PluginsCMDWorker worker = new PluginsCMDWorker();
		worker.setInputFile(filename);
		worker.setAggregate(1);
		worker.setRowsLimit(200);
		worker.setGraphWidth(700);
		worker.setGraphHeight(415);
		worker.setRelativeTimes(0);
		worker.setAutoScaleRows(0);

		{ // summary file
			worker.setPluginType("AggregateReport");
			worker.addExportMode(PluginsCMDWorker.EXPORT_CSV);
			worker.setOutputCSVFile(resultNameBase + "_Summary.csv");
			int result;
			if ((result = worker.doJob()) != 0) {
				throw new JMeterHandlerParseException(result);
			}
		}

		{ // graph files
			worker.addExportMode(PluginsCMDWorker.EXPORT_PNG);
			for (String graphMode : graphModes) {
				worker.setPluginType(graphMode);
				for (int i = 0; i < 2; i++) {
					worker.setSuccessFilter(i);
					final String resultNameFull = resultNameBase + "_" + graphMode + "+" + (i == 0 ? "Fail" : "Success") + ".png";
					worker.setOutputPNGFile(tempDirectory + resultNameFull);
					int result;
					if ((result = worker.doJob()) != 0) {
						throw new JMeterHandlerParseException(result);
					} else {
						System.out.print("Image creation for " + resultNameFull + " was successful");
					}
				}
			}
		}
	}

	private String setupFileName(String filename) {
		String resultNameBase = "RegexDidntMatch";
		Pattern pattern = Pattern.compile("_(\\d+-\\w+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(filename);
		if (matcher.find()) {
			resultNameBase = matcher.group(1);
		}
		return resultNameBase;
	}

	public void setupJMeter() throws JMeterHandlerSetupException {
		final String[] props = { "jmeter", "saveservice", "system", "upgrade", "user" };
		final String suffix = ".properties";
		try {
			final Path tempDir = Files.createTempDirectory("jmeter-csv-parser");
			setTempDir(tempDir.toString() + "/");
			new File(tempDir + "/bin").mkdir();
			for (String property : props) {
				final URL resourceUrl = Resources.getResource(property + suffix);
				final File tmpPropFile = new File(tempDir.toString() + "/bin", property + suffix);
				BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(tmpPropFile));
				bufferedWriter.write(Resources.toString(resourceUrl, Charsets.UTF_8));
				bufferedWriter.close();
			}
			System.out.println("nautilus " + getTempDir());
			JMeterUtils.loadJMeterProperties(tempDir.toString() + "/bin/jmeter.properties");
			JMeterUtils.setJMeterHome(tempDir.toString());
			JMeterUtils.setLocale(new Locale("ignoreResources"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new JMeterHandlerSetupException(e.getMessage());
		}
	}
}
