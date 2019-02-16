# service-template

As of 2019/01, this service-template has been un-deprecated. [doorctl](https://github.com/doordash/doorctl) is a scaffolding + injector generator that may be useful to be continued on if deemed sufficiently useful and worthwhile to maintain.

Tech stack:
 * [Python 3](https://docs.python.org/3/)
 * [Flask](http://flask.pocoo.org/)
 * [Docker](https://docs.docker.com/)
 * [Kubernetes](https://kubernetes.io/docs/home/)
 * [Helm](https://docs.helm.sh/)
    
Setup Docker to use Helm to deploy local builds into a local Kubernetes cluster: 
  1. Enable Kubernetes: Click on Docker whale icon > `Kubernetes` > `Enable local cluster`
  2. Select Context: `kubectl config use-context docker-for-desktop`<br>
     Note: If no context exists with the name `docker-for-desktop context`, then restart the cluster...<br>
     Docker whale icon > `Kubernetes` > `Disable local cluster` and then `Enable local cluster`.
  3. Install Helm: `brew install kubernetes-helm`
  4. Init Helm: `helm init`

To build a local Docker image: `make docker-build`

To deploy the Docker image *using* Helm to Kubernetes: `make local-deploy`

To check status *using* Helm: `make local-status`

To tail logs *using* Kubernetes: `make local-tail`

To stop and clean up *using* Helm: `make local-clean`

# Without Docker

Install requirements using pip3: `pip3 install -r requirements.txt`

To run using python3: `bash runlocal.sh`

To run using uWSGI: `bash runuwsgi.sh`

# Running locally

Run a server locally with Docker (use port 80) or without Docker (use port 5000) (you can also do it from PyCharm)

Sample request:

`GET` [http://localhost/](http://localhost/)

Sample response:

`Hello, World!`