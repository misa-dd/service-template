#!/bin/bash

nc -z localhost 80
retVal=$?

if [[ $retVal = 127 ]]
then
	echo "please install netcat to succeed (brew has it)"
	exit 1
elif [[ $retVal != 0 ]]
then
	echo "you don't seem to have the service running locally, try \"make run\""
	echo "if you have already done that take a look at \"docker logs service-template.web\" and see if the service is up and running yet"
	exit 2
else
	echo "service seems to be running"
fi

export SERVICE_URL="127.0.0.1:80"

if [[ -z "$ARTIFACTORY_USERNAME" ]]
then
	echo "ARTIFACTORY_USERNAME must be defined and exported"
fi
if [[ -z "$ARTIFACTORY_PASSWORD" ]]
then
        echo "ARTIFACTORY_PASSWORD must be defined and exported"
fi

if [[ -z "$(which python3.7)" ]]
then
	echo "python3.7 must be installed"
fi

export SERVICE_NAME='service-template'
export PRESSURE_VENV_PATH=/tmp/pressurevenv # or any location where you want to create the virtual environment
python3.7 -m venv ${PRESSURE_VENV_PATH} # create a python 3.6 virtual environment

source ${PRESSURE_VENV_PATH}/bin/activate # activate the virtual environment
VERSION=`git rev-parse HEAD`
pip3 install --extra-index-url https://${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD}@ddartifacts.jfrog.io/ddartifacts/api/pypi/pypi-local/simple/ doordash-pressure~=1.0


cd pressure

pip3 install -r requirements.txt # only needed if you specified additional requirements for your tests
pressure --data-file=${PRESSURE_VENV_PATH}/infra/local/data.yaml --data-file=infra/local/data.yaml --locust-args="--host http://${SERVICE_URL}/ -f tests/locustfile.py --csv=report --no-web -c 1000 -r 100 --run-time 5m"

# If you want want a web interface with live charts and stats, run the below command and enter the values values for users and requests per second respectively. The dashboard is available at localhost:8089
#pressure --data-file=${PRESSURE_VENV_PATH}/infra/local/data.yaml --data-file=infra/local/data.yaml --locust-args="--host http://${SERVICE_URL}/ -f tests/locustfile.py --csv=report"

# To deactivate the virtual environment
deactivate

cd ..
