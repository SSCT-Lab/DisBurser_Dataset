#!/bin/bash

srcName=elasticsearch-1.3.0-src
tar=elasticsearch-1.3.0.tar.gz
system=elasticsearch-1.3.0

cd ./$srcName
echo "current working directory: `pwd`"
mvn -DskipTests clean install
cp ./target/releases/$tar ../.

cd ..
tar zxvf $tar
rm -rf $tar
tar -zcvf $tar $system
rm -rf $system
