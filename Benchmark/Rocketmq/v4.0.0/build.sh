#!/bin/bash

def1=-D'RMQ_189'
def2=-D'RMQ_153'
def3=-D'RMQ_270'
cFile=inject.c
exeFile=inject
srcName=rocketmq-all-4.0.0-incubating-src
system=rocketmq-4.0.0-incubating
buildJar1=rocketmq-client-4.0.0-incubating.jar
buildJar2=rocketmq-common-4.0.0-incubating.jar
buildJar3=rocketmq-store-4.0.0-incubating.jar
tar=rocketmq-4.0.0-incubating.tar.gz

if [ -f $cFile ]
then
    gcc $def1 $def2 $def3 $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName/client
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$buildJar1 ../../

cd ../common
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$buildJar2 ../../

cd ../store
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$buildJar3 ../../

cd ..
cd ..
rm -rf $buildJar1 $buildJar2 $tar
cp $buildJar1 ./$system/lib/
cp $buildJar2 ./$system/lib/
cp $buildJar3 ./$system/lib/
tar -zcvf $tar $system
rm -rf $buildJar1 $buildJar2 $buildJar3
