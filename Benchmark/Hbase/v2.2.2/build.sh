#!/bin/bash

def1=-D'HB_23682'
def2=-D'HB_26114'
def3=-D'HB_24189'
cFile=inject.c
exeFile=inject
srcName=hbase-2.2.2-src
system=hbase-2.2.2
jar=hbase-procedure-2.2.2.jar
jar2=hbase-server-2.2.2.jar
jar3=hbase-common-2.2.2.jar
tar=hbase-2.2.2.tar.gz

if [ -f $cFile ]
then
    gcc $def1 $def2 $def3 $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName/hbase-procedure/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar ../../

cd ../hbase-server/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar2 ../../

cd ../hbase-common/
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/$jar3 ../../

cd ../../
echo "current working directory: `pwd`"
rm -rf $tar
cp ./$jar ./$system/lib/
cp ./$jar2 ./$system/lib/
cp ./$jar3 ./$system/lib/
tar -zcvf $tar $system
rm -rf $jar $jar2 $jar3
