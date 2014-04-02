#/bin/bash
#mvn clean install package
mvn clean package
#java -jar ./target/jmeter-csv-parser.jar -b=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140328-0600_STGREQ.csv -t=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140328-1136_SpanishLoadTest-90min.csv
java -jar ./target/jmeter-csv-parser.jar -b=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140401-2100_9-PM-TEST.csv -t=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140402-1227_HotRoll-3hr.csv
