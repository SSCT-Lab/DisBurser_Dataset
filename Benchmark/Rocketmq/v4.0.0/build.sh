#!/bin/bash

def=-D'RMQ_189'
cFile=inject.c
exeFile=inject
srcName=rocketmq-all-4.0.0-incubating-src
system=rocketmq-4.0.0-incubating
buildJar1=rocketmq-client-4.0.0-incubating.jar
buildJar2=rocketmq-common-4.0.0-incubating.jar
tar=rocketmq-4.0.0-incubating.tar.gz

if [ -f $injectFile ]
then
    gcc $def $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName/common
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$buildJar2 ../../

cd ../client
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$buildJar1 ../../

cd ..
cd ..
rm -rf $buildJar1 $buildJar2 $tar
cp $buildJar1 ./$system/lib/
cp $buildJar2 ./$system/lib/
tar -zcvf $tar $system
rm -rf $buildJar1 $buildJar2
