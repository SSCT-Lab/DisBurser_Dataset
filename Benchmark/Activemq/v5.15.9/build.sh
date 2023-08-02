#!/bin/bash

def1=-D'AMQ_8252'
def2=-D'AMQ_8104'
def3=-D'AMQ_7312'
cFile=inject.c
exeFile=inject
srcName=activemq-parent-5.15.9-src
system=activemq-5.15.9
libJar1=activemq-all-5.15.9.jar
libJar2=activemq-broker-5.15.9.jar
tar=activemq-5.15.9.tar.gz

if [ -f $cFile ]
then
    gcc $def1 $def2 $def3 $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName/activemq-all
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$libJar1 ../../

cd ../activemq-broker
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$libJar2 ../../

cd ..
cd ..
cp $libJar1 ./$system/mq1/
cp $libJar1 ./$system/mq2/
cp $libJar1 ./$system/mq3/
cp $libJar2 ./$system/mq1/lib/
cp $libJar2 ./$system/mq2/lib/
cp $libJar2 ./$system/mq3/lib/
tar -zcvf $tar $system
rm -rf $libJar1 $libJar2
