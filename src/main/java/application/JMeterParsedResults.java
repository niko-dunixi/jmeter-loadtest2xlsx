package application;

import java.util.Map;

public class JMeterParsedResults implements Comparable<JMeterParsedResults> {
	private int priority;
	private String testName;
	private Map<String, String[]> csvMap;
	private Map<String, byte[]> images;

	JMeterParsedResults(int priority, String testName, Map<String, String[]> csvMap, Map<String, byte[]> images) {
		this.priority = priority;
		this.testName = testName;
		this.csvMap = csvMap;
		this.images = images;
	}

	public int getPriority() {
		return priority;
	}

	public String getTestName() {
		return testName;
	}

	public Map<String, String[]> getCsvMap() {
		return csvMap;
	}

	public Map<String, byte[]> getImages() {
		return images;
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
