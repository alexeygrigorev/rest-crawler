#!/bin/bash

BUILD_ZIP_NAME='crawler-balancer-app.zip'

echo
echo 'Building the crawler...'
echo 

rm -r libs
mvn clean package -DskipTests

mv target/crawler-balancer*.jar libs/

rm $BUILD_ZIP_NAME
zip -r $BUILD_ZIP_NAME libs addresses.txt run.sh

echo 'DONE: the build is in' $BUILD_ZIP_NAME