#!/bin/bash

cd crawler
./build.sh

cd ..

cd balancer
./build.sh

cd ..

mv crawler/crawler-app.zip .
mv balancer/crawler-balancer-app.zip .
