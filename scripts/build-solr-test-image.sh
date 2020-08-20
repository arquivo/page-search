#!/bin/bash
set -e

# Defaults
INDEX_NAME=${INDEX_NAME:-pagesearch}
BUILD_CONTAINER_NAME=${BUILD_CONTAINER_NAME:-solr-build-image}
SOLR_FILES=${SOLR_FILES:-"$PWD/../solr/pagesearch-solr"}

SOLR_DOCKER_IMAGE=${SOLR_DOCKER_IMAGE:-solr:8.4.1}
SOLR_ARQUIVO_IMAGE=${SOLR_ARQUIVO_IMAGE:-"arquivo/$INDEX_NAME-solr-test"}

echo "Cleaning old solr-db-data"
rm -rf "$SOLR_FILES/solr-db-data"

echo "Launching docker-solr with our schema to be populated..."
docker run --name $BUILD_CONTAINER_NAME -d -v "$SOLR_FILES:/configsets/$INDEX_NAME" -v "$PWD/../solr/test-data:/test-data" \
-p 8983:8983 $SOLR_DOCKER_IMAGE solr-precreate $INDEX_NAME "/configsets/$INDEX_NAME/conf"

echo "Wait a few seconds to Solr start....."
sleep 5

echo "posting test-data/data.json to Solr...."
docker exec $BUILD_CONTAINER_NAME post -c $INDEX_NAME /test-data/data.json

echo "Copying the Solr database...."
docker cp "$BUILD_CONTAINER_NAME:/var/solr/data/$INDEX_NAME" "$SOLR_FILES/solr-db-data"

# clean up containers
docker rm -f $BUILD_CONTAINER_NAME

echo "Build arquivo/pagesearch-solr-test with the updated Solr Index"
docker build -t $SOLR_ARQUIVO_IMAGE "$PWD/../solr/"

echo "Publish it to Docker Hub..."
docker push $SOLR_DOCKER_IMAGE

echo "Done"
