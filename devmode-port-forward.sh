#!/usr/bin/env bash

#
# Forwards the 8080 port of the Medok cotainer from the Kubernetes cluster
# to localhost.
#
# Can be used for triggering reloads after code changes when developing Medok
#
# Start DevMode with "start-devmode-minikube.sh"
#

set -e

. build-config.sh "N"

kubectl port-forward deployment/medok 8080:8080 -n ${MEDOK_NAMESPACE}