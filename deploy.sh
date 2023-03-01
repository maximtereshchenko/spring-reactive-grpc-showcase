#!/usr/bin/env bash

set -Eeuo pipefail

terraform -chdir=./terraform apply --var-file secret.tfvars

declare -A OUTPUTS
OUTPUTS["users"]="users_ecr_repository_url"
OUTPUTS["api-gateway"]="api_gateway_ecr_repository_url"
for SERVICE in "${!OUTPUTS[@]}"; do
  REGISTRY=$(terraform -chdir=./terraform output --raw "${OUTPUTS[$SERVICE]}")
  if [ "$(docker image inspect "$REGISTRY:latest" >/dev/null 2>&1 || echo "no such image")" != "" ]; then
    docker build --build-arg module="$SERVICE" -t "$REGISTRY:latest" ./
  fi
  aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin "$REGISTRY"
  docker push "$REGISTRY:latest"
done

#curl -v -X POST -H 'Content-Type:application/json' "http://$(terraform -chdir=./terraform output --raw api_gateway_dns)/users" -d '{"username":"admin","password":"pass","userType":"ADMIN"}'
