#!/usr/bin/env bash

#
# Provides shared settings for build/development scripts
#
# Arguments:
#
# 1 - Whether loading the version number is needed or not
#


#
# Conditional export of a value: only if not already defined
#
function cexport()
{
    if [[ ! "${!1}" ]]
    then
        export ${1}="${2}";
    fi
}

set -e

VERSION_NEEDED=${1:-"Y"}

if [[ ${VERSION_NEEDED} == "Y" ]]
then
    echo "Loading project version from the POM"

    MVN_VERSION=$(mvn -q \
        -Dexec.executable=echo \
        -Dexec.args='${project.version}' \
        --non-recursive \
        exec:exec)

    cexport MEDOK_VERSION "${MVN_VERSION}"
fi

# The Kubernetes namespace into which Medok is deployed for development
# It is usually deployed into the Mailu namespace
cexport MEDOK_NAMESPACE "mailu"
