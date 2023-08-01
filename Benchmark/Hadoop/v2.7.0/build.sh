#!/bin/bash

def1=-D'HDFS_11379'
def2=-D'HDFS_10987'
def3=-D'HDFS_11609'
cFile=inject.c
exeFile=inject
srcName=hadoop-2.7.0-src
system=hadoop-2.7.0
jar=hadoop-hdfs-2.7.0.jar
tar=hadoop-2.7.0.tar.gz

if [ -f $cFile ]
then
    gcc $def1 $def2 $def3 $cFile -o $exeFile
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
