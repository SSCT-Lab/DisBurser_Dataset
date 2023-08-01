#!/bin/bash

def1=-D'HB_21920'
def2=-D'HB_23359'
def3=-D'HB_23693'
cFile=inject.c
exeFile=inject
srcName=hbase-1.4.8-src
system=hbase-1.4.8
jar=hbase-server-1.4.8.jar
jar2=hbase-client-1.4.8.jar
tar=hbase-1.4.8.tar.gz

if [ -f $cFile ]
then
    gcc $def1 $def2 $def3 $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName/hbase-server/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar ../../

cd ../hbase-client/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar2 ../../

cd ../../
echo "current working directory: `pwd`"
rm -rf $tar
cp ./$jar ./$system/lib/
cp ./$jar2 ./$system/lib/
tar -zcvf $tar $system
rm -rf $jar $jar2