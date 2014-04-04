package application;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.poi.util.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import kg.apc.jmeter.PluginsCMDWorker;

public class JMeterHandler implements Callable<JMeterParsedResults> {

	private final static Random rand = new Random();
	private volatile static boolean initialized = false;
	private volatile static int totalInstances = 0;
	private int priority = 0;
	private static String cacheDirectory;
	private final String rawResultsFilename;

	// private JMeterParsedResults parsedResults = null;

	public JMeterHandler(String filename) throws JMeterHandlerSetupException {
		rawResultsFilename = filename;
		priority = setupJMeter();
	}

	@Override
	public JMeterParsedResults call() throws JMeterHandlerParseException, InterruptedException {
		int min = 10, max = 30;
		Thread.sleep(rand.nextInt(max - min + 1) + min);
		return parseRawFile();
	}

	public synchronized JMeterParsedResults parseRawFile() throws JMeterHandlerParseException {
		final String testIdentifier = loadtestIdentifier(rawResultsFilename);
		final String testNamePrefix = loadtestName(rawResultsFilename);
		final File resultsDirectory = new File(cacheDirectory + testIdentifier);
		resultsDirectory.mkdirs();
		openResultsFolder(resultsDirectory);
		Map<String, String[]> csvSummary = parseSummary(testNamePrefix, resultsDirectory);
		Map<String, byte[]> allGraphs = parseGraphs(testNamePrefix, resultsDirectory);
		return new JMeterParsedResults(priority, testNamePrefix, csvSummary, allGraphs);
	}

	public static synchronized int setupJMeter() throws JMeterHandlerSetupException {
		if (!initialized) {
			final String[] props = { "jmeter", "saveservice", "system", "upgrade", "user" };
			final String suffix = ".properties";
			try {
				final Path tempDir = Files.createTempDirectory("jmeter-csv-parser");
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
				cacheDirectory = System.getProperty("user.home") + "/.jmeter-csv-parser/";
				initialized = true;
			} catch (IOException e) {
				e.printStackTrace();
				throw new JMeterHandlerSetupException(e.getMessage());
			}
		}
		return totalInstances++;
	}

	private void openResultsFolder(final File resultsDirectory) {
		try {
			Desktop.getDesktop().open(resultsDirectory);
		} catch (IOException e) {
		}
	}

	private Map<String, String[]> parseSummary(final String resultNamePrefix, final File resultsDirectory) throws JMeterHandlerParseException {
		Path path = Paths.get(resultsDirectory.toString() + "/" + resultNamePrefix + "_Summary.csv");
		if (!Files.exists(path)) {
			final PluginsCMDWorker worker = new PluginsCMDWorker();
			worker.setInputFile(rawResultsFilename);
			{ // summary file
				worker.setPluginType("AggregateReport");
				worker.addExportMode(PluginsCMDWorker.EXPORT_CSV);
				worker.setOutputCSVFile(path.toString());
				int result;
				if ((result = worker.doJob()) != 0) {
					throw new JMeterHandlerParseException(result);
				} else {
					System.out.print("Summary creation was successful");
				}
			}
		}
		Map<String, String[]> csvMap = new HashMap<String, String[]>();
		try (BufferedReader br = new BufferedReader(new FileReader(path.toFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] values = line.split(",");
				if (values[0].compareTo("sampler_label") == 0) {
					csvMap.put("CSVHEADER", values);
				} else {
					csvMap.put(values[0], values);
				}
			}
		} catch (IOException e) {
		}
		return csvMap;
	}

	private Map<String, byte[]> parseGraphs(final String resultNamePrefix, final File resultsDirectory) throws JMeterHandlerParseException {
		final String[] graphModes = { "TransactionsPerSecond", "ResponseTimesOverTime" };

		final PluginsCMDWorker worker = new PluginsCMDWorker();
		worker.setInputFile(rawResultsFilename);
		worker.setAggregate(1);
		worker.setRowsLimit(200);
		worker.setGraphWidth(700);
		worker.setGraphHeight(415);
		worker.setRelativeTimes(0);
		worker.setAutoScaleRows(0);

		// ArrayList<byte[]> resultingImages = new ArrayList<byte[]>();
		SortedMap<String, byte[]> resultingImages = new TreeMap<String, byte[]>(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2) * -1;
			}
		});
		{ // graph files
			worker.addExportMode(PluginsCMDWorker.EXPORT_PNG);
			for (String graphMode : graphModes) {
				worker.setPluginType(graphMode);
				for (int j = 0; j < 2; j++) {
					worker.setSuccessFilter(j);
					final String resultNameFull = resultNamePrefix + "_" + graphMode + "_" + (j == 0 ? "Fail" : "Success") + ".png";
					Path path = Paths.get(resultsDirectory.toString() + "/" + resultNameFull);
					if (!Files.exists(path)) {
						worker.setOutputPNGFile(path.toString());
						int result;
						if ((result = worker.doJob()) != 0) {
							throw new JMeterHandlerParseException(result);
						} else {
							System.out.print("Image creation for " + resultNameFull + " was successful");
						}
					}
					try (InputStream is = new FileInputStream(path.toFile())) {
						byte[] bytes = IOUtils.toByteArray(is);
						resultingImages.put(resultNameFull, bytes);
					} catch (IOException e) {
						throw new JMeterHandlerParseException(-1);
					}
				}
			}
		}
		return resultingImages;
	}

	public static String loadtestIdentifier(String filename) {
		try (FileInputStream fis = new FileInputStream(filename)) {
			return DigestUtils.md5Hex((InputStream) fis);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("MD5 couldn't be generated. Giving you a random string instead.");
		}
		return RandomStringUtils.randomAlphanumeric(32);
	}

	public static String loadtestName(String fullFilename) {
		String resultNameBase = fullFilename.substring(fullFilename.lastIndexOf('/') + 1, fullFilename.lastIndexOf('.'));
		if (resultNameBase.startsWith("JmeterRawResults_")) {
			resultNameBase = resultNameBase.substring(17);
		}
		return resultNameBase;
	}
}
