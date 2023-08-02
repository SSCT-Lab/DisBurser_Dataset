#!/bin/bash

def1=-D'ES_6866'
def2=-D'ES_7210'
cFile=inject.c
exeFile=inject
srcName=elasticsearch-1.2.1-src
tar=elasticsearch-1.2.1.tar.gz
system=elasticsearch-1.2.1

if [ -f $cFile ]
then
    gcc $def1 $def2 $cFile -o $exeFile
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
