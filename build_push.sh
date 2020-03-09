#!/usr/bin/env bash

env=${EL_ENV}
if [[ ! $env ]]; then
  env="test"
fi
projects="gzh"
module="gzh_backend"

set -eo pipefail

docker build -t "fec/${projects}/${module}:${env}" .
docker tag fec/gzh/${module}:${env} 192.168.0.5:5000/fec/${projects}/${module}:${env}
docker push 192.168.0.5:5000/fec/${projects}/${module}:${env}

