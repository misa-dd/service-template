# service-template

* [Introduction](README.md#introduction)
* [Prerequisites for Local Development](README.md#prerequisites-for-local-development)
* [Build and Deploy](README.md#build-and-deploy)
* [Verify](README.md#verify)
  * [Run Pulse Tests](README.md#run-pulse-tests)
  * [Health Check](README.md#health-check)
  * [Health Check Response](README.md#health-check-response)
  * [Sample Request](README.md#sample-request)
  * [Sample Response](README.md#sample-response)
  * [Sample Request with name](README.md#sample-request-with-name)
  * [Sample Response with name](README.md#sample-response-with-name)
* [Appendix](README.md#appendix)

## Introduction

Reference project for setting up a Python/Flask service to run in a DoorDash kubernetes cluster.

* [Design and API Reference](DESIGN.md "Title")
* [Operating Guide](OPERATING.md "Title")

Tech stack:
 * [Flask](http://flask.pocoo.org/)
 * [Python 3](https://hub.docker.com/_/python)
 * [Docker](https://docs.docker.com/)
 * [Kubernetes](https://kubernetes.io/docs/home/)
 * [Argo Rollouts](https://argoproj.github.io/argo-rollouts/)
 * [Helm](https://docs.helm.sh/)
 * [Terraform](https://www.terraform.io/docs/)
 * [Debian Buster Slim](https://packages.debian.org/buster/slim)

## Prerequisites for Local Development

The following sections assume that you have completed the steps in the
[New-Engineer-Setup-Guide](https://github.com/doordash/doordash-eng-wiki/blob/master/docs/New-Engineer-Setup-Guide.md).

## Build and Deploy

To become familiar with how to clone, build, and run Microservices locally, complete the steps for [Microservices in the
New-Engineer-Setup-Guide](https://github.com/doordash/doordash-eng-wiki/blob/master/docs/New-Engineer-Setup-Guide.md#microservices).

## Verify

### Run Pulse Tests

```bash
# Build the Pulse image
make docker-build-pulse

# Run Pulse once using docker run
# NOTE: you may need to modify the make local-run-pulse target in _infra/infra.mk to set the environment variables needed by your tests
make local-run-pulse

# Deploy Pulse to Kubernetes and run Pulse every minute
make local-deploy-pulse

# Tail the logs of the Pulse pod
make local-tail-pulse 
# Note: Press CTRL+C to quit tailing logs
```

### Run Pressure Tests

```bash
# Build the Pressure image
make docker-build-pressure

# Start the Pressure test
# Note: This will stop any running pressure tests
make local-deploy-pressure

# Tail the logs of the Pressure master pod
make local-tail-pressure-master
# Note: Press CTRL+C to quit tailing logs

# Tail the logs of a Pressure worker pod
make local-tail-pressure-worker
# Note: Press CTRL+C to quit tailing logs
```

### Health Check

`curl -v ` [http://localhost/health](http://localhost/health)

### Health Check Response

```
< HTTP/1.1 200 OK
< Server: gunicorn/19.9.0
< Date: Fri, 06 Mar 2020 20:08:04 GMT
< Connection: keep-alive
< Content-Type: text/html; charset=utf-8
< Content-Length: 2
< 
OK
```

### Sample Request

`curl -v ` [http://localhost/](http://localhost/)

### Sample Response

```
< HTTP/1.1 200 OK
< Server: gunicorn/19.9.0
< Date: Fri, 06 Mar 2020 20:05:12 GMT
< Connection: keep-alive
< Content-Type: text/html; charset=utf-8
< Content-Length: 46
< 
Hello, World! I am running version localbuild
```

### Sample Request with name

`curl -v ` [http://localhost/?name=Mundo](http://localhost/?name=Mundo)

### Sample Response with name

```
< HTTP/1.1 200 OK
< Server: gunicorn/19.9.0
< Date: Fri, 06 Mar 2020 20:07:18 GMT
< Connection: keep-alive
< Content-Type: text/html; charset=utf-8
< Content-Length: 46
< 
Hello, Mundo! I am running version localbuild
```

## Secrets

service-template leverages the [secrets module](https://github.com/doordash/doordash-python-lib/tree/master/doordash_lib/secrets) from `doordash-python-lib` to access secrets. The source of the underlying secrets depends on the environment in which the service is running.

### Local

When running the service locally via docker-compose, the `secrets.json` file which lives in the root of the repo is mounted to the path that the secrets module expects. Adding secrets to `secrets.json` will make them available to the service when running locally.

### Staging/Production

When running the service in staging or production, secrets are retrieved from Vault and made available to the secrets module. See the [Vault user manual](https://docs.google.com/document/d/15hLzvxM21lMbD-qbgR946o8v7d3zb_tBQRKniQWpRLI) for more information on configuring a service with Vault.


## Appendix

### Using this project for your new microservice
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
[https://cloudbees.build.doordash.red/](https://cloudbees.build.doordash.red/)
make a minor modification, create a PR, and merge it.

If that doesn't work, you may also need to add `engineering` and `infrastructure` as collaborators with write access to
your GitHub repository.

### Running Locally without Docker or using docker-compose

Install requirements using pip3: `sudo pip3 install -r requirements.txt`

To run using python3: `bash runlocal.sh`

To run using gunicorn: `bash rungunicorn.sh`

To run using docker-compose: `docker-compose up -d`

To stop using docker-compose: `docker-compose down`

To rebuild using docker-compose: `docker-compose up -d --build`

