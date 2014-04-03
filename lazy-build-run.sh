#/bin/bash
#mvn clean install package
mvn clean package
java -jar ./target/jmeter-csv-parser.jar -b=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140401-2100_9-PM-TEST.csv -t=/home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140402-1730_RetestOfSolr.csv
#java -jar ./target/jmeter-csv-parser.jar /home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140401-2100_9-PM-TEST.csv /home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140402-2100_9-PM-TEST_summary.csv
