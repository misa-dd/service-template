#!/bin/bash
# INFRA HEADER -- DO NOT EDIT. metadata: {"checksum": "9f7300c71816ebda84a30c73c7bdf3e5"}

set -euo pipefail

# Inject sensitive environment variables from doordash-secret-service.
SECRETS=$(doordash-secret get --service service-template)  # fail script if doordash-secret call errors
eval $SECRETS

export INSTANCE_LOCAL_IP="$(wget -qO- -t 1 169.254.169.254/latest/meta-data/local-ipv4 || echo "unknown-ip")"

exec "$@"