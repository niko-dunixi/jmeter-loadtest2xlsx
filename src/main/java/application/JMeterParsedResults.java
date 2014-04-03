package application;

import java.util.ArrayList;
import java.util.Map;

public class JMeterParsedResults implements Comparable<JMeterParsedResults> {
	private int priority;
	private String testName;
	private Map<String, String[]> csvMap;
	private ArrayList<byte[]> images;

	JMeterParsedResults(int priority, String testName, Map<String, String[]> csvMap, ArrayList<byte[]> images) {
		this.priority = priority;
		this.testName = testName;
		this.csvMap = csvMap;
		this.images = images;
	}

	@Override
	public int compareTo(JMeterParsedResults o) {
		return this.priority - o.priority;
	}

	@Override
	public String toString() {
		return "JMeterParsedResults [priority=" + priority + ", testName=" + testName + ", csvMapNull?=" + (csvMap == null) + ", imagesNull?=" + (images == null) + "]";
	}
}
