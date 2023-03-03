resource "aws_sns_topic" "events_sns_topic" {
  name = "events"
}

resource "aws_sns_topic_subscription" "events_sns_topic_subscription" {
  topic_arn            = aws_sns_topic.events_sns_topic.arn
  protocol             = "sqs"
  endpoint             = aws_sqs_queue.orders_read_side_sqs_queue.arn
  raw_message_delivery = true
}