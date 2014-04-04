#/bin/bash
#mvn clean install package
mvn clean package
touch lazy-build-run.log
(java -jar ./target/jmeter-csv-parser.jar /home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140401-2100_9-PM-TEST.csv /home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140402-1730_RetestOfSolr.csv /home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140403-1345_LATTE-206.csv >> lazy-build-run.log 2>&1) &
tail -f -n 45 lazy-build-run.log
