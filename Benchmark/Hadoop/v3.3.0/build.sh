#!/bin/bash

def=-D'HDFS_15398'
cFile=inject.c
exeFile=inject
srcName=hadoop-3.3.0-src
system=hadoop-3.3.0
jar=hadoop-hdfs-client-3.3.0.jar
tar=hadoop-3.3.0.tar.gz

if [ -f $cFile ]
then
    gcc $def $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName/hadoop-hdfs-project/hadoop-hdfs-client/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar ../../../

cd ../../../
echo "current working directory: `pwd`"
rm -rf $tar
cp ./$jar ./$system/share/hadoop/hdfs/
tar -zcvf $tar $system
rm -rf $jar
