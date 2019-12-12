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
export PULSE_VENV_PATH=/tmp/pulsevenv
python3.7 -m venv ${PULSE_VENV_PATH}

source ${PULSE_VENV_PATH}/bin/activate
VERSION=`git rev-parse HEAD`
pip3 install --extra-index-url https://${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD}@ddartifacts.jfrog.io/ddartifacts/api/pypi/pypi-local/simple/ doordash-pulse~=2.1


cd pulse

pip3 install -r requirements.txt # only needed if you specified additional requirements for your tests
pulse --data-file=${PULSE_VENV_PATH}/infra/local/data.yaml --data-file=infra/local/data.yaml
# If you want to disable stdout and stderr capturing allowing log messages to be printed to the console
#pulse --data-file=${PULSE_VENV_PATH}/infra/local/data.yaml --data-file=infra/local/data.yaml --pytest-args="-s"
# If you want to specify additional pytest args
#pulse --data-file=${PULSE_VENV_PATH}/infra/local/data.yaml --data-file=infra/local/data.yaml --pytest-args="-x --tb=no"
# If you want to run all tests from one file
#pulse --data-file=${PULSE_VENV_PATH}/infra/local/data.yaml --data-file=infra/local/data.yaml --pytest-args="-x --tb=no [relative-path-to-test-file]"
# If you want to run one specific test from one file
#pulse --data-file=${PULSE_VENV_PATH}/infra/local/data.yaml --data-file=infra/local/data.yaml --pytest-args="-x --tb=no [relative-path-to-test-file]::[test-function-name]"
# To deactivate the virtual environment
deactivate

cd ..
