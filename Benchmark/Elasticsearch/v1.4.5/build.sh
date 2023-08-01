#!/bin/bash

def=-D'ES_8321'
cFile=inject.c
exeFile=inject
srcName=elasticsearch-1.4.5-src
tar=elasticsearch-1.4.5.tar.gz
system=elasticsearch-1.4.5

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
cp ./target/releases/$tar ../.

cd ..
tar zxvf $tar
rm -rf $tar
tar -zcvf $tar $system
rm -rf $system
