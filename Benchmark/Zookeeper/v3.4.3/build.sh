#!/bin/bash

def1=-D'ZK_1419'
def2=-D'ZK_1489'
def3=-D'ZK_1621'
cFile=inject.c
exeFile=inject
srcName=zookeeper-3.4.3-src
tar=zookeeper-3.4.3.tar.gz

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
ant tar
cp ./build/$tar ../

