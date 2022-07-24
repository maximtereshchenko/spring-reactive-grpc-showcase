#!/bin/sh
docker image rm -f orders-read-side
docker image rm -f orders-write-side
docker image rm -f users
docker build --build-arg module=orders-read-side -t orders-read-side ./
docker build --build-arg module=orders-write-side -t orders-write-side ./
docker build --build-arg module=users -t users ./