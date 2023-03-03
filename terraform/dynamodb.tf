resource "aws_dynamodb_table" "users_dynamodb_table" {
  name         = "users"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"
  attribute {
    name = "id"
    type = "S"
  }
}

resource "aws_dynamodb_table" "unique_usernames_dynamodb_table" {
  name         = "uniqueUsernames"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "username"
  attribute {
    name = "username"
    type = "S"
  }
}

resource "aws_dynamodb_table" "events_dynamodb_table" {
  name         = "events"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "aggregateId"
  range_key    = "version"
  attribute {
    name = "aggregateId"
    type = "S"
  }
  attribute {
    name = "version"
    type = "N"
  }
}

resource "aws_dynamodb_table" "carts_dynamodb_table" {
  name         = "carts"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "userId"
  attribute {
    name = "userId"
    type = "S"
  }
}

resource "aws_dynamodb_table" "items_dynamodb_table" {
  name         = "items"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"
  attribute {
    name = "id"
    type = "S"
  }
}

resource "aws_dynamodb_table" "top_ordered_items_dynamodb_table" {
  name         = "topOrderedItems"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "id"
  attribute {
    name = "id"
    type = "S"
  }
}

resource "aws_dynamodb_table" "ordered_items_dynamodb_table" {
  name         = "orderedItems"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "userId"
  attribute {
    name = "userId"
    type = "S"
  }
}