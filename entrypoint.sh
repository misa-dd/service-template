#!/bin/bash

set -euxo pipefail # https://vaneyckt.io/posts/safer_bash_scripts_with_set_euxo_pipefail/

# Temporarily reduce metadata service timeout for local development
# export AWS_METADATA_SERVICE_TIMEOUT=7

export DATABASE_PASSWORD="comment this line out to simulate when secrets are not found"

# Inject sensitive environment variables from doordash-secret-service.
time SECRETS=$(doordash-secret get --service service-template) || true
eval ${SECRETS}

DATABASE_PASSWORD=${DATABASE_PASSWORD:-}
if [ -z "${DATABASE_PASSWORD}" ] ; then
  echo "Retrying in 10 seconds"
  sleep 10
  time SECRETS=$(doordash-secret get --service service-template) || true
  eval ${SECRETS}
  [ -z "${DATABASE_PASSWORD}" ] && { echo "DATABASE_PASSWORD is unset. Aborting container startup"; exit 1; }
fi

export INSTANCE_LOCAL_IP="$(wget -qO- -t 1 -T 5 169.254.169.254/latest/meta-data/local-ipv4 || echo "unknown-ip")"

exec "$@"
