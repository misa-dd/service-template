#!/bin/bash

export INSTANCE_LOCAL_IP=`curl -s 169.254.169.254/latest/meta-data/local-ipv4`

/root/docker/setup-splunk.sh

exec "$@"
