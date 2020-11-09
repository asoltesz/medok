# Developing / Building the operator project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

This guide expects a Docker-capable, Linux based development environment with access to Docker HUB. 

If you are on a different operating system, please use a VM. 

## Running the application in Developer Mode

Seet the [Developer Mode](developer-mode.md) page for details.

## Building a native executable (for release)

The native executable version of this operator will have very low memory and CPU footprint (as opposed to normal Java applications).

### Set the version number

In the pom.xml ( the "version" tag)

### Execute the build script

~~~
./build-release.sh
~~~

### Push the results to Docker HUB

~~~
./release-to-docker-hub.sh
~~~
