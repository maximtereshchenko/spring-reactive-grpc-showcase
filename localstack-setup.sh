#!/usr/bin/env bash

set -euo pipefail

create_queue() {
  awslocal sqs create-queue --queue-name "$1"
}

create_topic() {
  awslocal sns create-topic --name "$1"
}

link_queue_and_topic() {
  awslocal sns subscribe --attributes RawMessageDelivery=true --topic-arn "$1" --protocol sqs --notification-endpoint "$2"
}

create_dynamo_db_table() {
  if [ "$#" -gt 3 ]; then
    awslocal dynamodb create-table --billing-mode PAY_PER_REQUEST --table-name "$1" --attribute-definitions "AttributeName=$2,AttributeType=$3" "AttributeName=$4,AttributeType=$5" --key-schema "AttributeName=$2,KeyType=HASH" "AttributeName=$4,KeyType=RANGE"
  else
    awslocal dynamodb create-table --billing-mode PAY_PER_REQUEST --table-name "$1" --attribute-definitions "AttributeName=$2,AttributeType=$3" --key-schema "AttributeName=$2,KeyType=HASH"
  fi
}

AWS_REGION="us-east-1"
TOPIC="events"
QUEUE="orders-read-side"
create_topic "${TOPIC}"
create_queue "${QUEUE}"
link_queue_and_topic "arn:aws:sns:${AWS_REGION}:000000000000:${TOPIC}" "arn:aws:sns:${AWS_REGION}:000000000000:${QUEUE}"
create_dynamo_db_table "users" "id" "S"
create_dynamo_db_table "uniqueUsernames" "username" "S"
create_dynamo_db_table "events" "aggregateId" "S" "version" "N"
create_dynamo_db_table "carts" "userId" "S"
create_dynamo_db_table "items" "id" "S"
create_dynamo_db_table "topOrderedItems" "id" "S"
create_dynamo_db_table "orderedItems" "userId" "S"
