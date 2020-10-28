#!/bin/bash

set -euxo pipefail # https://vaneyckt.io/posts/safer_bash_scripts_with_set_euxo_pipefail/

_term() {
  echo "Caught SIGTERM signal!"
  echo "Forwarding SIGTERM to $child process"
  kill -TERM "$child" 2>/dev/null
  wait "$child"
}

_hup() {
  echo "caught SIGHUP signal!"
  echo "Forwarding SIGHUP to $child process"
  kill -HUP "$child" 2>/dev/null
  wait "$child"
}

_quit() {
  echo "caught SIGQUIT signal!"
  echo "Forwarding SIGQUIT to $child process"
  kill -QUIT "$child" 2>/dev/null
  wait "$child"
}

_int() {
  echo "caught SIGINT signal!"
  echo "Forwarding SIGINT to $child process"
  kill -INT "$child" 2>/dev/null
  wait "$child"
}

if [[ "${ENVIRONMENT}" == "local" ]] ; then
  # Reduce metadata service timeout for local development
  export AWS_METADATA_SERVICE_TIMEOUT=7
fi

export INSTANCE_LOCAL_IP="$(wget -qO- -t 1 -T 5 169.254.169.254/latest/meta-data/local-ipv4 || echo "unknown-ip")"

trap _term SIGTERM
trap _quit SIGQUIT
trap _hup  SIGHUP
trap _int  SIGINT

"$@" &

child=$!
wait "$child"

if [[ -d /sidecarutil ]]; then
  touch /sidecarutil/stopruntime
fi
