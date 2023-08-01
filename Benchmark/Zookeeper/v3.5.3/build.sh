#!/bin/bash

def=-D'ZK_2355'
cFile=inject.c
exeFile=inject
srcName=zookeeper-3.5.3-src
tar=zookeeper-3.5.3-beta.tar.gz

if [ -f $injectFile ]
then
    gcc $def $cFile -o $exeFile
    echo "gcc compile success"
    ./$exeFile
else
  echo "Error: $cFile not found !"
fi

cd ./$srcName
echo "current working directory: `pwd`"
ant tar
cp ./build/$tar ../

