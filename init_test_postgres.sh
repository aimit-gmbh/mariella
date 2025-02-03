#!/usr/bin/env bash

set -euo pipefail

./cleanup_docker.sh || true

docker run -d -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
