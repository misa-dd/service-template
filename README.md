# Service Template

As of 2019/01, this service template has been un-deprecated. [doorctl](https://github.com/doordash/doorctl) is a scaffolding + injector generator that may be useful to be continued on if deemed sufficiently useful and worthwhile to maintain.

Tech stack:
 * [Python 3](https://docs.python.org/3/)
 * [Flask](http://flask.pocoo.org/)
 * [Docker](https://docs.docker.com/)
 * [Kubernetes](https://kubernetes.io/docs/home/)
 * [Helm](https://docs.helm.sh/)
    
Setup Docker to use Helm to deploy local builds into a local Kubernetes cluster: 
 1. Enable Kubernetes: Click on Docker whale icon > Kubernetes > Enable local cluster
 2. Select Context: `kubectl config use-context docker-for-desktop`
 3. Install Helm: `brew install kubernetes-helm`
 4. Init Helm: `helm init`

To build a local Docker image: `make docker-build`

To run *using* Docker: `make docker-deploy-local`

To check status *using* Docker: `make docker-status-local`

To tail logs *using* Docker: `make docker-tail-local`

To stop *using* Docker: `make docker-clean-local`

# Running locally

Run a server locally with or without Docker (you can also do it from PyCharm)

Sample request:

`GET` [http://localhost/](http://localhost/)

Sample response:

`Hello, World!`