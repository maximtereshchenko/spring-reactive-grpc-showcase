#!/bin/sh
docker image rm -f orders-read-side
docker image rm -f orders-write-side
docker image rm -f users
docker image rm -f api-gateway
docker build --build-arg module=orders-read-side -t orders-read-side ./
docker build --build-arg module=orders-write-side -t orders-write-side ./
docker build --build-arg module=users -t users ./
docker build --build-arg module=api-gateway -t api-gateway ./