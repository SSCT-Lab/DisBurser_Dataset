#!/bin/bash

def=-D'ZK_4508'
cFile=inject.c
exeFile=inject
srcName=apache-zookeeper-3.7.1
system=apache-zookeeper-3.7.1-bin
tar=apache-zookeeper-3.7.1-bin.tar.gz

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
