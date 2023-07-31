#!/bin/bash

def1=-D'HDFS_14987'
def2=-D'HDFS_15443'
cFile=inject.c
exeFile=inject
srcName=hadoop-3.1.2-src
system=hadoop-3.1.2
jar=hadoop-hdfs-3.1.2.jar
tar=hadoop-3.1.2.tar.gz

if [ -f $injectFile ]
then
    gcc $def1 $def2 $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName/hadoop-hdfs-project/hadoop-hdfs/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar ../../../

cd ../../../
echo "current working directory: `pwd`"
rm -rf $tar
cp ./$jar ./$system/share/hadoop/hdfs/
tar -zcvf $tar $system
rm -rf $jar
