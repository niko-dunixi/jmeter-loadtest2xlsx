#/bin/bash
touch lazy-build-run.log
(java -jar ./target/jmeter-csv-parser.jar /home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140404-0600_STGREQ.csv /home/paulbaker/LoadTest/Results/JMeter/JMeter/JmeterRawResults_20140404-1131.csv >> lazy-build-run.log 2>&1) &
tail -f -n 45 lazy-build-run.log
