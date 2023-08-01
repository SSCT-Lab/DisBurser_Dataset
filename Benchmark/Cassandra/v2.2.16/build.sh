#!/bin/bash

def1=-D'CAS_14365'
def2=-D'CAS_13528'
def3=-D'CAS_15814'
cFile=inject.c
exeFile=inject
srcName=apache-cassandra-2.2.16-src
system=apache-cassandra-2.2.16
buildJar=apache-cassandra-2.2.16-SNAPSHOT.jar
libJar=apache-cassandra-2.2.16.jar
tar=apache-cassandra-2.2.16.tar.gz

if [ -f $cFile ]
then
    gcc $def1 $def2 $def3 $cFile -o $exeFile
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
