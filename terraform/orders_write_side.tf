resource "aws_ecr_repository" "orders_write_side_ecr_repository" {
  name                 = local.orders_write_side_name
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

resource "aws_ecr_lifecycle_policy" "orders_write_side_ecr_lifecycle_policy" {
  repository = aws_ecr_repository.orders_write_side_ecr_repository.name
  policy     = jsonencode({
    rules = [
      {
        rulePriority = 1
        action       = {
          type = "expire"
        }
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 1
        }
      }
    ]
  })
}

resource "aws_lb" "orders_write_side_load_balancer" {
  load_balancer_type = "application"
  internal           = true
  subnets            = [aws_subnet.public_subnet_a.id, aws_subnet.public_subnet_b.id]
  security_groups    = [aws_security_group.security_group.id]
}

resource "aws_lb_target_group" "orders_write_side_load_balancer_target_group" {
  vpc_id           = aws_vpc.vpc.id
  port             = local.container_port
  protocol         = "HTTPS"
  protocol_version = "GRPC"
  target_type      = "ip"
  health_check {
    protocol = "HTTP"
    path     = local.health_check_path
    port     = local.health_check_port
    matcher  = 0
  }
}

resource "aws_lb_listener" "orders_write_side_application_load_balancer_listener" {
  load_balancer_arn = aws_lb.orders_write_side_load_balancer.arn
  port              = local.container_port
  protocol          = "HTTPS"
  certificate_arn   = aws_acm_certificate.server_certificate.arn
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.orders_write_side_load_balancer_target_group.arn
  }
}

resource "aws_ecs_task_definition" "orders_write_side_ecs_task_definition" {
  family                   = local.orders_write_side_name
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = aws_iam_role.iam_role.arn
  container_definitions    = jsonencode([
    {
      name         = local.orders_write_side_name
      image        = "${aws_ecr_repository.orders_write_side_ecr_repository.repository_url}:latest"
      essential    = true
      portMappings = [
        {
          protocol      = "HTTP"
          containerPort = local.container_port
          hostPort      = local.container_port
        }
      ]
      environment : [
        {
          name  = "APPLICATION_DYNAMODB_ENDPOINT"
          value = "dynamodb.${var.aws_region}.amazonaws.com"
        },
        {
          name  = "APPLICATION_SNS_ENDPOINT"
          value = "sns.${var.aws_region}.amazonaws.com"
        },
        {
          name  = "CLOUD_AWS_REGION_STATIC"
          value = var.aws_region
        },
        {
          name  = "AWS_ACCESS_KEY_ID"
          value = var.aws_task_access_key
        },
        {
          name  = "AWS_SECRET_KEY"
          value = var.aws_task_secret_key
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options   = {
          awslogs-group         = aws_cloudwatch_log_group.cloudwatch_log_group.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = local.orders_write_side_name
        }
      }
    }
  ])
}

resource "aws_ecs_service" "orders_write_side_ecs_service" {
  depends_on                         = [aws_lb_listener.orders_write_side_application_load_balancer_listener]
  name                               = "${local.orders_write_side_name}-service"
  cluster                            = aws_ecs_cluster.ecs_cluster.id
  task_definition                    = aws_ecs_task_definition.orders_write_side_ecs_task_definition.arn
  desired_count                      = 1
  deployment_minimum_healthy_percent = 50
  deployment_maximum_percent         = 200
  launch_type                        = "FARGATE"
  scheduling_strategy                = "REPLICA"
  network_configuration {
    security_groups  = [aws_security_group.security_group.id]
    subnets          = [aws_subnet.public_subnet_a.id]
    assign_public_ip = true
  }
  load_balancer {
    target_group_arn = aws_lb_target_group.orders_write_side_load_balancer_target_group.arn
    container_name   = local.orders_write_side_name
    container_port   = local.container_port
  }
}