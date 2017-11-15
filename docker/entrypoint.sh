#!/bin/bash

# Inject sensitive environment variables from doordash-secret-service.
# TODO (service owner) replace "service-template" with service name as registered in doordash-secret-service
eval `doordash-secret get --service service-template`

export INSTANCE_LOCAL_IP=`curl -s 169.254.169.254/latest/meta-data/local-ipv4`

exec "$@"
