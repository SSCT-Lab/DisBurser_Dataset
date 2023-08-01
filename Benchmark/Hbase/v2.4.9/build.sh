#!/bin/bash

def=-D'HB_26742'
cFile=inject.c
exeFile=inject
srcName=hbase-2.4.9-src
system=hbase-2.4.9
jar=hbase-server-2.4.9.jar
tar=hbase-2.4.9.tar.gz


if [ -f $cFile ]
then
    gcc $def $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName/hbase-server/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar ../../

cd ../../
echo "current working directory: `pwd`"
rm -rf $tar
cp ./$jar ./$system/lib/
tar -zcvf $tar $system
rm -rf $jar
