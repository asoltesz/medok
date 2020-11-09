# Developer Mode

Utilizing the Quarkus developer mode for continuous, live development inside a container running on Minikube.

# Start Minikube

~~~
minikube start
~~~
...or the way you start your Minikube instance normally


# Compile the application JAR and build the DevMode docker image

~~~
./start-devmode-minikube.sh
~~~

This will:
- Build the application as a JAR
- Build the Docker image into the cache of the Docker daemon of your Minikube instance, so the image is ready for deployment 
- Creates the "medok" namespace if it doesn't exists (for development)
- Creates the necessary RBAC objects in Minikube
- Creates the Medok deployment in the "medok" namespace (thus starts Medok in DevMode)


# Forward the http port to localhost

~~~
./devmode-port-forward.sh
~~~

# Start the remote developer mode

This will start listening to local changes in the source code, compile and forward them to the remote container:
~~~
./mvnw quarkus:remote-dev -Dquarkus.live-reload.url=http://localhost:8080
~~~
NOTE: In case of class changes the container will restart.


# Check the container output

Follow/Trail the output (typically in a separate terminal):
~~~
kubectl logs -l app=medok -n medok --follow
~~~
