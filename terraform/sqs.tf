resource "aws_sqs_queue" "orders_read_side_sqs_queue" {
  name = "orders-read-side"
}

resource "aws_sqs_queue_policy" "orders_read_side_sqs_queue_policy" {
  queue_url = aws_sqs_queue.orders_read_side_sqs_queue.url
  policy    = jsonencode({
    Version   = "2012-10-17"
    Statement = [
      {
        Action    = ["sqs:SendMessage"]
        Effect    = "Allow"
        Sid       = ""
        Principal = {
          Service = "sns.amazonaws.com"
        }
        Resource = aws_sqs_queue.orders_read_side_sqs_queue.arn
      }
    ]
  })
}