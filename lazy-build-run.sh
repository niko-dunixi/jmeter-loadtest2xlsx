#/bin/bash
#mvn clean install package
mvn clean package
java -jar ./target/jmeter-csv-parser-0.0.1-SNAPSHOT.jar -b=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140324-0500_BASELINE.csv -t=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140324-1122_LATTE-186.csv
