#!/bin/bash

def1=-D'CAS_13464'
def2=-D'CAS_15297'
def3=-D'CAS_16836'
def4=-D'CAS_17628'
cFile=inject.c
exeFile=inject
srcName=apache-cassandra-3.11.6-src
system=apache-cassandra-3.11.6
buildJar=apache-cassandra-3.11.6-SNAPSHOT.jar
libJar=apache-cassandra-3.11.6.jar
tar=apache-cassandra-3.11.6.tar.gz

if [ -f $cFile ]
then
    gcc $def1 $def2 $def3 $def4 $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName
echo "current working directory: `pwd`"
ant

cp ./build/$buildJar ../
cd ..
rm -rf $libJar $tar
mv $buildJar $libJar
cp $libJar ./$system/lib/
tar -zcvf $tar $system
rm -rf $libJar
