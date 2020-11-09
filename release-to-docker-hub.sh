#!/usr/bin/env bash

set -e

# Loading the version number of the release
. build-config.sh

echo
echo "Image to be pushed to Docker HUB: soltesza/medok:${MEDOK_VERSION}"
echo

docker push soltesza/medok:${MEDOK_VERSION}

