#!/bin/bash
set -e

# refs http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-instance-metadata.html#instancedata-data-retrieval
echo export DOCKER_HOST_HOSTNAME=`curl -s 169.254.169.254/latest/meta-data/local-ipv4` >> /etc/profile
echo export INSTANCE_ID=`curl -s 169.254.169.254/latest/meta-data/instance-id` >> /etc/profile

source /etc/profile

/app/docker/setup-splunk.sh
