#!/bin/bash

def=-D'ZK_4473'
cFile=inject.c
exeFile=inject
srcName=apache-zookeeper-3.8.0
system=apache-zookeeper-3.8.0-bin
tar=apache-zookeeper-3.8.0-bin.tar.gz

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
mvn -DskipTests clean install
cp ./zookeeper-assembly/target/$tar ../

cd ..
tar -zxvf $tar
tar -zcvf $tar $system
rm -rf $system
