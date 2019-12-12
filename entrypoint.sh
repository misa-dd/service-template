#!/bin/bash

set -euxo pipefail # https://vaneyckt.io/posts/safer_bash_scripts_with_set_euxo_pipefail/

_term() {
  echo "Caught SIGTERM signal!"
  kill -TERM "$child" 2>/dev/null
}

_hup() {
  echo "caught SIGHUP signal!"
  kill -HUP "$child" 2>/dev/null
}

_quit() {
  echo "caught SIGQUIT signal!"
  kill -QUIT "$child" 2>/dev/null
}

_int() {
  echo "caught SIGINT signal!"
  kill -INT "$child" 2>/dev/null
}

if [[ "${ENVIRONMENT}" == "local" ]] ; then
  # Reduce metadata service timeout for local development
  export AWS_METADATA_SERVICE_TIMEOUT=7
  export DATABASE_PASSWORD="comment this line out to simulate when secrets are not found"
fi

eval `doordash-secret get --service service-template` || echo "doordash-secret failed, are we local?" 1>&2

DATABASE_PASSWORD=${DATABASE_PASSWORD:-}

if [[ -z "${DATABASE_PASSWORD}" ]] ; then
  echo "Retrying in 10 seconds"
  sleep 10
  eval `doordash-secret get --service service-template` || true
  [[ -z "${DATABASE_PASSWORD}" ]] && { echo "DATABASE_PASSWORD is unset. Aborting container startup"; exit 1; }
fi

export INSTANCE_LOCAL_IP="$(wget -qO- -t 1 -T 5 169.254.169.254/latest/meta-data/local-ipv4 || echo "unknown-ip")"


trap _term SIGTERM
trap _quit SIGQUIT
trap _hup  SIGHUP
trap _int  SIGINT

"$@" &

child=$!
wait "$child"
