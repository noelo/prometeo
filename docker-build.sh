#!/usr/bin/env bash
#
# builds the prometeo-runner docker image from source
#

# creates a build folder
mkdir build

# clones and builds the prometeo jar file
cd build
git clone https://github.com/prometeo-cloud/prometeo.git
cd prometeo
mvn package
cd ../../

# build docker images
cd build
git clone https://github.com/prometeo-cloud/prometeo-docker.git

# if base jre8 images does not exists the it creates it
if [[ "$(docker images -q jre:8 2> /dev/null)" == "" ]]; then
  cd prometeo-docker/jre8
  sh build.sh
  cd ../../
fi

cd prometeo-docker/prometeo
cp ../../prometeo/target/prometeo*.jar .
sh build.sh
cd ../../../

#  cleaning up directories
rm -rf build