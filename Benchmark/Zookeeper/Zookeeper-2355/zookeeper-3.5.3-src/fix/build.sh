#!/bin/bash

tar=zookeeper-3.5.3-beta.tar.gz

cd ..
echo "current working directory: `pwd`"
ant tar
cp ./build/$tar ../

