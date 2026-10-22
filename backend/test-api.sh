#!/bin/sh
set -eu
curl -sS http://localhost:8080/api/sensors/latest
printf "\n\n"
curl -sS http://localhost:8080/api/market/listings
printf "\n"
