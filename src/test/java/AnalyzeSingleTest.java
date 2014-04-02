import static org.junit.Assert.*;

import org.junit.Test;

import application.JMeterHandler;
import application.JMeterHandlerParseException;
import application.JMeterHandlerSetupException;


public class AnalyzeSingleTest {

	@Test
	public void test() throws JMeterHandlerSetupException, JMeterHandlerParseException {
		JMeterHandler jmeter = new JMeterHandler();
		String baslineFolder = jmeter.parseRawFile("/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140328-0600_STGREQ.csv");
	}

}
