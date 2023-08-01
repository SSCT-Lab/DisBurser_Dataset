#!/bin/bash

def1=-D'HB_26901'
def2=-D'HB_26027'
def3=-D'HB_27027'
cFile=inject.c
exeFile=inject
srcName=hbase-2.4.11-src
system=hbase-2.4.11
jar=hbase-server-2.4.11.jar
jar2=hbase-client-2.4.11.jar
jar3=hbase-http-2.4.11.jar
jar4=hbase-rest-2.4.11.jar
jar5=hbase-thrift-2.4.11.jar
tar=hbase-2.4.11.tar.gz


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

cd ../hbase-http/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar3 ../../

cd ../hbase-rest/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar4 ../../

cd ../hbase-thrift/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar5 ../../

cd ../../
echo "current working directory: `pwd`"
rm -rf $tar
cp ./$jar ./$system/lib/
cp ./$jar2 ./$system/lib/
cp ./$jar3 ./$system/lib/
cp ./$jar4 ./$system/lib/
cp ./$jar5 ./$system/lib/
tar -zcvf $tar $system
rm -rf $jar $jar2 $jar3 $jar4 $jar5
