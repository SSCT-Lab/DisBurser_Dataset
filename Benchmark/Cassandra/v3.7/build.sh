#!/bin/bash

def=-D'CAS_12424'
cFile=inject.c
exeFile=inject
srcName=apache-cassandra-3.7-src
system=apache-cassandra-3.7
buildJar=apache-cassandra-3.7-SNAPSHOT.jar
libJar=apache-cassandra-3.7.jar
tar=apache-cassandra-3.7.tar.gz

if [ -f $cFile ]
then
    gcc $def $cFile -o $exeFile
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
