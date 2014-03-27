#/bin/bash
#mvn clean install package
mvn clean package
java -jar ./target/jmeter-csv-parser-0.0.1-SNAPSHOT.jar /home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140324-0500_BASELINE.csv
