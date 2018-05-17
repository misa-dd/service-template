#!/bin/bash
# INFRA HEADER -- DO NOT EDIT. metadata: {"checksum": "2863f63b62679a8643e52f11eb8d562b"}

set -euo pipefail

# Inject sensitive environment variables from doordash-secret-service.
eval "$(doordash-secret get --service service-template)"

export INSTANCE_LOCAL_IP="$(wget -qO- -t 1 169.254.169.254/latest/meta-data/local-ipv4 || echo "unknown-ip")"

exec "$@"