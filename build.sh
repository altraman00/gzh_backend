#!/usr/bin/env bash

set -eo pipefail
#mvn clean compile package -Pdev -DskipTests=true
docker build -t "fec/gzh/gzh_backend:latest" .
