# service-template

* [Introduction](README.md#introduction)
* [Prerequisites for Local Development](README.md#prerequisites-for-local-development)
  * [1. Setup Kubernetes](README.md#1-setup-kubernetes)
  * [2. Install Deployment Tools](README.md#2-install-deployment-tools)
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
 * [Terraform](https://www.terraform.io/docs/)
 * [Debian](https://packages.debian.org/buster/slim)


## Prerequisites for Local Development

The following steps assume that you have completed the steps in the
[New-Engineer-Setup-Guide](https://github.com/doordash/doordash-eng-wiki/blob/master/docs/New-Engineer-Setup-Guide.md).

### 1. Setup Kubernetes

Setup a local Kubernetes cluster with Helm v2 to deploy local builds:
  1. Make sure you are running Docker Desktop version 2.1.0.5 or above.
  2. Enable Kubernetes: Click on Docker whale icon > `Preferences...` > `Kubernetes` > `Enable Kubernetes`
  3. Select Context: `kubectl config use-context docker-for-desktop`<br>
     Note: If no context exists with the name `docker-for-desktop context`, then restart the cluster...<br>
     Docker whale icon > `Kubernetes` > `Disable local cluster` and then `Enable local cluster`.
  4. Install Helm: `brew install kubernetes-helm && brew unlink kubernetes-helm && brew install https://raw.githubusercontent.com/Homebrew/homebrew-core/ee94af74778e48ae103a9fb080e26a6a2f62d32c/Formula/kubernetes-helm.rb`
  5. Init Helm: `helm init`

### 2. Install Deployment Tools

Install tools required to deploy to a local Kubernetes cluster:
  1. Install Terraform: `brew install terraform`
  2. Install Argo Rollouts:
     ```bash
     pushd ..
     git clone https://github.com/doordash/infra2.git
     cd infra2/infra/bluegreen
     kubectl create namespace argo-rollouts
     helm install --wait --name argo-rollouts ./chart --namespace argo-rollouts -f values_staging.yaml
     popd
     ```
  3. Install Argo Rollouts Kubectl Plugin:
     ```bash
     curl -LO https://github.com/argoproj/argo-rollouts/releases/download/v0.6.0/kubectl-argo-rollouts-darwin-amd64
     chmod +x ./kubectl-argo-rollouts-darwin-amd64
     sudo mv ./kubectl-argo-rollouts-darwin-amd64 /usr/local/bin/kubectl-argo-rollouts
     kubectl argo rollouts version
     ```
  4. Clone common-pipelines repo:
     ```bash
     pushd ..
     git clone https://github.com/doordash/common-pipelines.git
     popd
      ```
  5. Install jq: `brew install jq`


## Build and Deploy

All of the following should be executed within the `service-template` directory...

To build a local Docker image: `make docker-build`

To deploy the Docker image *using* Terraform with Helm to Kubernetes: `make local-deploy`

To check status *using* Kubernetes: `make local-get-all`

 * Note: you are looking for the following:
 ```bash
NAME                                       DESIRED   CURRENT   UP-TO-DATE   AVAILABLE
rollout.argoproj.io/service-template-web   1         1         1            1
 ```

To tail logs *using* Kubernetes: `make local-tail`

 * Note: Press CTRL+C to quit tailing logs

To stop and clean up *using* Helm: `make local-clean`


## Running Locally without Docker or using docker-compose

Install requirements using pip3: `sudo pip3 install -r requirements.txt`

To run using python3: `bash runlocal.sh`

To run using gunicorn: `bash rungunicorn.sh`

To run using docker-compose: `docker-compose up -d`

To stop using docker-compose: `docker-compose down`

To rebuild using docker-compose: `docker-compose up -d --build`


## Verify

Run a server locally with Kubernetes or docker-compose (use port 80) or without Docker (use port 5000) (you can also do it from PyCharm)

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
make a minor modification, create a PR, and merge it.

If that doesn't work, you may also need to add `engineering` and `infrastructure` as collaborators with write access to
your GitHub repository.
