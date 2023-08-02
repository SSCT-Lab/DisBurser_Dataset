#!/bin/bash

def1=-D'KA_7763'
def2=-D'KA_7941'
def3=-D'KA_9254'
cFile=inject.c
exeFile=inject
srcName=kafka-2.0.0-src
system=kafka_2.11-2.0.0
tgz=kafka_2.11-2.0.0.tgz
tar=kafka_2.11-2.0.0.tar.gz

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
./gradlew clean releaseTarGz -x signArchives
cp ./core/build/distributions/$tgz ../

cd ..
tar -zxvf $tgz
rm -rf $tar
tar -zcvf $tar $system
rm -rf $system $tgz
