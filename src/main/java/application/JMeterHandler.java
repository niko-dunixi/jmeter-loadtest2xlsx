package application;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.jmeter.util.JMeterUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import kg.apc.jmeter.PluginsCMDWorker;

public class JMeterHandler {
	
	private JMeterParsedResults parsedResults;
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

	public String parseRawFile(String filename) throws JMeterHandlerParseException {
		final String userHomeDir = System.getProperty("user.home") + "/";
		final String testIdentifier = loadtestIdentifier(filename);
		final File resultsDirectory = new File(userHomeDir + ".jmeter-csv-parser/" + testIdentifier + "/");
		resultsDirectory.mkdirs();
		// openResultsFolder(resultsDirectory);
		parseSummary(filename, resultsDirectory);
		parseGraphs(filename, resultsDirectory);
		return resultsDirectory.toString() + "/";
	}

	private void openResultsFolder(final File resultsDirectory) {
		try {
			Desktop.getDesktop().open(resultsDirectory);
		} catch (IOException e) {
		}
	}

	private void parseGraphs(String filename, File resultsDirectory) throws JMeterHandlerParseException {
		final String[] graphModes = { "TransactionsPerSecond", "ResponseTimesOverTime" };
		final String resultNameBase = loadtestName(filename);

		final PluginsCMDWorker worker = new PluginsCMDWorker();
		worker.setInputFile(filename);
		worker.setAggregate(1);
		worker.setRowsLimit(200);
		worker.setGraphWidth(700);
		worker.setGraphHeight(415);
		worker.setRelativeTimes(0);
		worker.setAutoScaleRows(0);

		{ // graph files
			worker.addExportMode(PluginsCMDWorker.EXPORT_PNG);
			for (String graphMode : graphModes) {
				worker.setPluginType(graphMode);
				for (int i = 0; i < 2; i++) {
					worker.setSuccessFilter(i);
					final String resultNameFull = resultNameBase + "_" + graphMode + "_" + (i == 0 ? "Fail" : "Success") + ".png";
					Path path = Paths.get(resultsDirectory.toString() + "/" + resultNameFull);
					if (Files.exists(path)) {
						System.out.println(path.toString() + " already exists. Skipping.");
						continue;
					}
					// worker.setOutputPNGFile(resultsDirectory.toString() +
					// resultNameFull);
					worker.setOutputPNGFile(path.toString());
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

	private void parseSummary(String filename, File resultsDirectory) throws JMeterHandlerParseException {
		final String resultNameBase = loadtestName(filename);

		Path path = Paths.get(resultsDirectory.toString() + "/" + resultNameBase + "_Summary.csv");
		if (Files.exists(path)) {
			System.out.println(path.toString() + " already exists. Skipping.");
			return;
		}

		final PluginsCMDWorker worker = new PluginsCMDWorker();
		worker.setInputFile(filename);
		{ // summary file
			worker.setPluginType("AggregateReport");
			worker.addExportMode(PluginsCMDWorker.EXPORT_CSV);
			// worker.setOutputCSVFile(resultsDirectory.toString() +
			// resultNameBase + "_Summary.csv");
			worker.setOutputCSVFile(path.toString());
			int result;
			if ((result = worker.doJob()) != 0) {
				throw new JMeterHandlerParseException(result);
			} else {
				System.out.print("Summary creation was successful");
			}
		}
	}

	public static String loadtestIdentifier(String filename) {
		try (FileInputStream fis = new FileInputStream(filename)) {
			return DigestUtils.md5Hex((InputStream) fis);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("MD5 couldn't be fully generated. This file is probably still being written to. Returning random string instead.");
		}
		return RandomStringUtils.randomAlphanumeric(32);
	}

	public static String loadtestName(String fullFilename) {
		String resultNameBase = "RegexDidntMatch";
		Pattern pattern = Pattern.compile("_(\\d+-\\w+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(fullFilename);
		if (matcher.find()) {
			resultNameBase = matcher.group(1);
		}
		// with our filename scheme this should always match, unless you're
		// being really stupid.
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
			JMeterUtils.loadJMeterProperties(tempDir.toString() + "/bin/jmeter.properties");
			JMeterUtils.setJMeterHome(tempDir.toString());
			JMeterUtils.setLocale(new Locale("ignoreResources"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new JMeterHandlerSetupException(e.getMessage());
		}
	}
}
