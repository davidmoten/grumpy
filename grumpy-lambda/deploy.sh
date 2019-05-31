#!/bin/bash
set -e
MODE=$1
if [ $# -eq 0 ]
  then
    echo "Usage: ./deploy.sh <mode>"
    exit 1
fi
# aws:deployFileS3@file
if [ "$2" == 'skip' ]; then
  COMMAND="" 
  PARAMS="$3 $4 $5 $6 $7 $8"
else
  COMMAND='aws:deployFileS3@file' 
  PARAMS="$2 $3 $4 $5 $6 $7 $8"
fi
mvn clean package $COMMAND  aws:deployCf@cf aws:deployRestApi@api -Dmode=$MODE $PARAMS 
