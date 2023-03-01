#!/usr/bin/env bash

set -Eeuo pipefail

terraform -chdir=./terraform apply --var-file secret.tfvars

USERS_REGISTRY=$(terraform -chdir=./terraform output --raw "users_ecr_repository_url")
docker build --build-arg module=users -t "$USERS_REGISTRY:latest" ./
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin "$USERS_REGISTRY"
docker push "$USERS_REGISTRY:latest"

API_GATEWAY_REGISTRY=$(terraform -chdir=./terraform output --raw "api_gateway_ecr_repository_url")
docker build --build-arg module=api-gateway -t "$API_GATEWAY_REGISTRY:latest" ./
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin "$API_GATEWAY_REGISTRY"
docker push "$API_GATEWAY_REGISTRY:latest"

#curl -v -X POST -H 'Content-Type:application/json' "http://$(terraform -chdir=./terraform output --raw api_gateway_dns)/users" -d '{"username":"admin","password":"pass","userType":"ADMIN"}'
