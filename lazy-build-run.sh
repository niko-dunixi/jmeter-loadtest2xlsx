#/bin/bash
#mvn clean install package
mvn clean package
#java -jar ./target/jmeter-csv-parser.jar -b=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140328-0600_STGREQ.csv -t=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140328-1136_SpanishLoadTest-90min.csv
java -jar ./target/jmeter-csv-parser.jar -b=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140328-0600_STGREQ.csv -t=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140328-1758_Solr4-ctTax-Pets.csv

