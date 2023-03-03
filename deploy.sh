#!/usr/bin/env bash

set -Eeuo pipefail

terraform -chdir=./terraform apply --var-file secret.tfvars

declare -A SERVICES_REPOSITORIES
SERVICES_REPOSITORIES["users"]="users_ecr_repository_url"
SERVICES_REPOSITORIES["api-gateway"]="api_gateway_ecr_repository_url"
SERVICES_REPOSITORIES["orders-write-side"]="orders_write_side_ecr_repository_url"
SERVICES_REPOSITORIES["orders-read-side"]="orders_read_side_ecr_repository_url"
for SERVICE in "${!SERVICES_REPOSITORIES[@]}"; do
  REPOSITORY=$(terraform -chdir=./terraform output --raw "${SERVICES_REPOSITORIES[$SERVICE]}")
  if [ "$(docker image inspect "$REPOSITORY:latest" >/dev/null 2>&1 || echo "no such image")" != "" ]; then
    docker build --build-arg module="$SERVICE" -t "$REPOSITORY:latest" ./
  fi
  aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin "$REPOSITORY"
  docker push "$REPOSITORY:latest"
done
