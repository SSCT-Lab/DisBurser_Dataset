#!/bin/bash

def1=-D'KA_13852'
def2=-D'KA_14024'
def3=-D'KA_14797'
cFile=inject.c
exeFile=inject
srcName=kafka-3.2.0-src
system=kafka_2.13-3.2.0
tgz=kafka_2.13-3.2.0.tgz
tar=kafka_2.13-3.2.0.tar.gz

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
./gradlew clean releaseTarGz
cp ./core/build/distributions/$tgz ../

cd ..
tar -zxvf $tgz
rm -rf $tar
tar -zcvf $tar $system
rm -rf $system $tgz
