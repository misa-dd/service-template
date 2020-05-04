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
 * [Python 3](https://docs.python.org/3/)
 * [Flask](http://flask.pocoo.org/)
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
> make docker-build-pulse  # build the pulse img

# For your service, you may need to modify the make local-run-pulse target to set the environment variables needed by your tests
> make local-run-pulse  # run Pulse once

> make local-deploy-pulse  # deploy Pulse to Kubernetes and run Pulse every minute
> make local-tail-pulse  # tail the logs from the Pulse pod
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
[https://generaljenkins.doordash.com/](https://generaljenkins.doordash.com/)
and [https://deployjenkins.doordash.com/](https://deployjenkins.doordash.com/),
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
