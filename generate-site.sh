#!/bin/bash
set -e
mvn site
cd ../davidmoten.github.io
mkdir -p grumpy
git pull
cp -r ../grumpy/target/site/* grumpy/
git add .
git commit -am "update site reports"
git push
