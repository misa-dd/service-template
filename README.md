# service-template

* [Introduction](README.md#introduction)
* [Prerequisites](README.md#prerequisites)
  * [1. Install Maven](README.md#1-install-maven)
  * [2. Artifactory](README.md#2-artifactory)
  * [3. Maven Settings](README.md#3-maven-settings)
  * [4. Setup Docker](README.md#4-setup-docker)
* [Build and Deploy](README.md#build-and-deploy)
* [Running Locally without Docker or using docker-compose](README.md#running-locally-without-docker-or-using-docker-compose)
* [Verify](README.md#verify)
  * [Health Check](README.md#health-check)
  * [Sample Request](README.md#sample-request)
  * [Sample Response](README.md#sample-response)
* [Using this project for your new microservice](README.md#using-this-project-for-your-new-microservice)

## Introduction

Reference project for setting up a Python/Flask service to run in a DoorDash kubernetes cluster.

* [Design and API Reference](DESIGN.md "Title")
* [Operating Guide](OPERATING.md "Title")

Tech stack:
 * [Python 3](https://docs.python.org/3/)
 * [Flask](http://flask.pocoo.org/)
 * [Docker](https://docs.docker.com/)
 * [Kubernetes](https://kubernetes.io/docs/home/)
 * [Helm](https://docs.helm.sh/)
 * [Alpine Linux](https://alpinelinux.org/) to minimize the size of the container


## Prerequisites

The following steps assume that you have completed the steps in the 
[New-Engineer-Setup-Guide](https://github.com/doordash/doordash-eng-wiki/blob/master/docs/New-Engineer-Setup-Guide.md).
 
### 1. Setup Docker
    
Setup Docker to use Helm to deploy local builds into a local Kubernetes cluster: 
  1. Enable Kubernetes: Click on Docker whale icon > `Preferences...` > `Kubernetes` > `Enable Kubernetes`
  2. Select Context: `kubectl config use-context docker-for-desktop`<br>
     Note: If no context exists with the name `docker-for-desktop context`, then restart the cluster...<br>
     Docker whale icon > `Kubernetes` > `Disable local cluster` and then `Enable local cluster`.
  3. Install Helm: `brew install kubernetes-helm`
  4. Init Helm: `helm init`


## Build and Deploy

All of the following should be executed within the `service-template` directory...

To build a local Docker image: `make docker-build`

To deploy the Docker image *using* Helm to Kubernetes: `make local-deploy`

To check status *using* Helm: `make local-status`

 * Note: you are looking for the following
 ```bash
NAME                  READY  UP-TO-DATE  AVAILABLE  AGE
service-template-web  1/1    1           1          24s
 ```

To tail logs *using* Kubernetes: `make local-tail`

 * Note: Press CTRL+C to quit tailing logs

To stop and clean up *using* Helm: `make local-clean`


## Running Locally without Docker or using docker-compose

Install requirements using pip3: `pip3 install -r requirements.txt`

To run using python3: `bash runlocal.sh`

To run using uWSGI: `bash runuwsgi.sh`

To run using docker-compose: `docker-compose up -d`

To stop using docker-compose: `docker-compose down`

To rebuild using docker-compose: `docker-compose up -d --build`


## Verify

Run a server locally with Docker (use port 80) or without Docker (use port 5000) (you can also do it from PyCharm)

### Health Check

`curl -v ` [http://localhost/health](http://localhost/health)

### Sample Request

`curl -v ` [http://localhost/](http://localhost/)

### Sample Response

```
< HTTP/1.1 200 OK
< Server: nginx/1.14.2
< Date: Tue, 11 Jun 2019 23:23:01 GMT
< Content-Type: text/html; charset=utf-8
< Content-Length: 13
< Connection: keep-alive
<
Hello, World!
```


## Using this project for your new microservice
```
# Remove the remote pointing to service-template
git remote remove origin

# Create a new empty repo using https://github.com/organizations/doordash/repositories/new
# add remote pointing to your new empty repo
git remote add origin <your-new-empty-repo-url>

# Verify that the remote is correct
git remote -v

# Push to master
# Note: you may need to use the -f flag if you created the repo with any default files.
git push -u origin master


```

To coerce Jenkins to create your service directory and jobs in
[https://generaljenkins.doordash.com/](https://generaljenkins.doordash.com/)
and [https://deployjenkins.doordash.com/](https://deployjenkins.doordash.com/),
make a minor modification to the 3 Jenkins groovy files, create a PR, and merge it.

If that doesn't work, you may also need to add `engineering` and `infrastructure` as collaborators with write access to
your GitHub repository.
