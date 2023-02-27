#!/usr/bin/env bash

set -euo pipefail

create_queue() {
  awslocal "--endpoint-url=http://localhost:4566" sqs create-queue --queue-name "$1"
}

create_topic() {
  awslocal "--endpoint-url=http://localhost:4566" sns create-topic --name "$1"
}

link_queue_and_topic() {
  awslocal "--endpoint-url=http://localhost:4566" sns subscribe --attributes RawMessageDelivery=true --topic-arn "$1" --protocol sqs --notification-endpoint "$2"
}

AWS_REGION="us-east-1"
TOPIC="events"
create_topic "${TOPIC}"
QUEUES=("users" "orders-read-side" "orders-write-side")
for QUEUE in "${QUEUES[@]}"; do
  create_queue "${QUEUE}"
  link_queue_and_topic "arn:aws:sns:${AWS_REGION}:000000000000:${TOPIC}" "arn:aws:sns:${AWS_REGION}:000000000000:${QUEUE}"
done
