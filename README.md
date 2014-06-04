##JMeter Loadtest 2 Xlsx##
###Overview###
This is something I feel should be redone. It gets the job done, but I wrote this during a time period I was constantly sick and feverish. Letting mortality get to me, I think this could be optimized and flow can be made a little more clear as well
A really simple loadtest analyzer that can chain as many loadtests as the command line will let you enter.

###Usage###
Usage is rediculously simple. From the command line simply list the loadtests you want analyzed *in the order* you want them compared respectively to each other.

For example, running this:
``java -jar jmeter-csv-parser.jar JmeterRawResults_Baseline.csv JmeterRawResults_CodeChangeTest.csv``

Will output an excel document that lists the stats of `JmeterRawResults_Baseline.csv` followed by the stats of `JmeterRawResults_CodeChangeTest.csv`. In between the stats will be two extra columns. The first shows the percentage of the new average reponse time over the precedeing average response time. So if your average response times double, you will see 200%. If your average response times decrease by half you will see 50%. The second column will show the delta of the total errors. So if half your calls begin to fail compared to your baseline, you will see 50%. If your calls were previously failing, but have recovered due to whatever change is being tested; you will see -50%.

The average/average column also has some additional formatting. If you see an increase in response times by 10%, you will see yellow text as a warning. If you see an increase in response times by 20% you will see red failure text. (Of course your requirements may vary, in which case you may have to make changes for your situation)

Below all the stats, you will see four graphs that JMeter produces. They are Transactions per Second (sucessful), Transactions per Second (failures), Response Times Over Time (sucessful), Response Times Over Time (failures).
