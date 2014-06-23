#!/bin/bash
set -e
mvn site
mvn site:stage
cd ../davidmoten.github.io
git pull
mkdir grumpy
cp -r ../grumpy/target/staging/* grumpy/
git add .
git commit -am "update site reports"
git push
