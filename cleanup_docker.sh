#!/usr/bin/env bash

set -euo pipefail

docker rm -f "$(docker ps -a -q)"
docker volume rm "$(docker volume ls -q)"
