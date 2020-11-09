#!/usr/bin/env bash

#
# Starts DevMode
#


deploymentExists() {

    local deployment=$1

    # Checking the deployment, dropping error messages
    local description="$(kubectl describe deployment ${deployment} -n ${MEDOK_NAMESPACE} 2> /dev/null)"

    if [[ "${description}" ]]
    then
        # deployment is present in the cluster
        return
    fi

    false
}

IS_BUILD_NEEDED=${1:-"Y"}

set -e

. build-config.sh

if [[ ${IS_BUILD_NEEDED} == "Y" ]]
then
    ./mvnw package -Dquarkus.package.type=mutable-jar

    # Switching to the Docker daemon of Minikube
    eval $(minikube docker-env)

    # Building the developer image into minikube
    docker build -f src/main/docker/Dockerfile.fast-jar -t soltesza/medok:develop .
fi

cd deployment/devmode

kubectl apply -f namespace.yaml

kubectl apply -f rbac.yaml -n ${MEDOK_NAMESPACE}

# Preemptively deleting since the pod will not rebuild if it is already deployed
# just because a newer image was placed in the Docker local cache
if deploymentExists "medok"
then
    echo "Deleting deployment, before re-creating"
    kubectl delete -f deployment.yaml -n ${MEDOK_NAMESPACE}
fi

kubectl apply -f deployment.yaml  -n ${MEDOK_NAMESPACE}

echo "Waiting 10s for the deployment to stabilize"

# Following logs from the deployment
sleep 10s

echo "Attaching to the DevMode Medok pod"
kubectl logs -l app=medok -n ${MEDOK_NAMESPACE} --follow