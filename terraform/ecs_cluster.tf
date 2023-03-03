resource "aws_ecs_cluster" "ecs_cluster" {
  name = "spring-reactive-grpc-showcase"
}

resource "aws_cloudwatch_log_group" "cloudwatch_log_group" {}

resource "aws_iam_role" "iam_role" {
  assume_role_policy = jsonencode({
    Version   = "2012-10-17"
    Statement = [
      {
        Action    = "sts:AssumeRole"
        Effect    = "Allow"
        Sid       = ""
        Principal = {
          Service = "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "iam_role_policy_attachment" {
  role       = aws_iam_role.iam_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}
