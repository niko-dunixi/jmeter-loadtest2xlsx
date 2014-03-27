#/bin/bash
wget -O /tmp/rsyntaxtextarea_2.5.1.zip "http://downloads.sourceforge.net/project/rsyntaxtextarea/rsyntaxtextarea/2.5.1/rsyntaxtextarea_2.5.1.zip?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Frsyntaxtextarea%2Ffiles%2Frsyntaxtextarea%2F2.5.1%2F&ts=1395955584&use_mirror=hivelocity"
unzip -d /tmp/ /tmp/rsyntaxtextarea_2.5.1.zip
mvn install:install-file -Dfile=/tmp/rsyntaxtextarea.jar -DgroupId=com.fifesoft -DartifactId=rsyntaxtextarea -Dversion=2.5.1 -Dpackaging=jar
