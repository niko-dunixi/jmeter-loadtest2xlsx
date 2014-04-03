package application;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import application.JMeterHandlerParseException;
import application.JMeterHandlerSetupException;

public class Application {

	private Application(Set<String> filenames) throws JMeterHandlerSetupException, InterruptedException, ExecutionException {
		List<JMeterParsedResults> results = parserThreading(filenames);
		for (JMeterParsedResults j : results) {
			System.out.println(j);
		}
		if (results.size() > 0) {
			ExcelGenerator xlsx = new ExcelGenerator(results);
		}
	}

	private List<JMeterParsedResults> parserThreading(Set<String> filenames) throws JMeterHandlerSetupException, InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newCachedThreadPool();
		List<JMeterHandler> jmeterHandelers = new ArrayList<JMeterHandler>();
		for (String filename : filenames) {
			jmeterHandelers.add(new JMeterHandler(filename));
		}
		List<Future<JMeterParsedResults>> allParsers = executor.invokeAll(jmeterHandelers);
		List<JMeterParsedResults> results = new ArrayList<JMeterParsedResults>();
		for (Future<JMeterParsedResults> parser : allParsers) {
			results.add(parser.get());
		}
		executor.shutdown();
		return results;
	}

	public static void main(String[] args) throws YourArgumentIsInvalid, JMeterHandlerSetupException, InterruptedException, ExecutionException {
		Set<String> filenames = new TreeSet<String>();
		for (String filename : args) {
			filenames.add(filename);
		}
		if (filenames.size() <= 1) {
			throw new YourArgumentIsInvalid();
		}
		new Application(filenames);
	}
}