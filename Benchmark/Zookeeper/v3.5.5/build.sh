#!/bin/bash

def=-D'ZK_3479'
cFile=inject.c
exeFile=inject
srcName=zookeeper-3.5.5-src
system=apache-zookeeper-3.5.5-bin
tar=apache-zookeeper-3.5.5-bin.tar.gz

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
