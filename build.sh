#!/bin/bash

BUILD_ZIP_NAME='crawler-app.zip'

echo
echo 'Building the crawler...'
echo 

rm -r libs
mvn clean package -DskipTests

mv target/crawler-api*.jar libs/
cp -R src/main/resources/config .

rm $BUILD_ZIP_NAME
zip -r $BUILD_ZIP_NAME libs config phantomjs run.sh
rm -r config

echo 'DONE: the build is in' $BUILD_ZIP_NAME