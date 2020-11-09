#!/usr/bin/env bash

set -e

# Loading the version number of the release
. build-config.sh

echo
echo "Image to be pushed to Docker HUB: asoltesz/medok:${MEDOK_VERSION}"
echo

docker push asoltesz/medok:${MEDOK_VERSION}

