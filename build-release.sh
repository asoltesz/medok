#!/usr/bin/env bash

set -e

. build-config.sh

# Building Linux x86-64 native application image
./mvnw package -Pnative -Dnative-image.docker-build=true -Dquarkus.package.type=native

# Building the docker image
docker build -f src/main/docker/Dockerfile.native -t soltesza/medok:${MEDOK_VERSION} .

echo
echo "Image built: soltesza/medok:${MEDOK_VERSION}"
echo
echo "Build success."