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
		humanReadableTitles();
	}

	public void humanReadableTitles() {
		String[] tmpMap = getCsvMap().get("CSVHEADER");
		tmpMap[0] = getTestName();
		tmpMap[1] = "Count";
		tmpMap[2] = "Avg";
		tmpMap[3] = "Med";
		// tmpMap[4] = "90% Line";
		tmpMap[5] = "Min";
		tmpMap[6] = "Max";
		tmpMap[7] = "Err%";
		tmpMap[8] = "Rate";
		// tmpMap[9] = "Bandwidth";
		tmpMap[10] = "StdDev";
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
