resource "aws_ecr_repository" "api_gateway_ecr_repository" {
  name                 = local.api_gateway_name
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

resource "aws_ecr_lifecycle_policy" "api_gateway_ecr_lifecycle_policy" {
  repository = aws_ecr_repository.api_gateway_ecr_repository.name
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

resource "aws_lb" "api_gateway_load_balancer" {
  load_balancer_type = "application"
  internal           = false
  subnets            = [aws_subnet.public_subnet_a.id, aws_subnet.public_subnet_b.id]
  security_groups    = [aws_security_group.security_group.id]
}

resource "aws_lb_target_group" "api_gateway_load_balancer_target_group" {
  vpc_id      = aws_vpc.vpc.id
  port        = local.container_port
  protocol    = "HTTP"
  target_type = "ip"
  health_check {
    protocol = "HTTP"
    path     = local.api_gateway_health_check_path
    port     = local.health_check_port
    matcher  = "200"
  }
}

resource "aws_lb_listener" "api_gateway_application_load_balancer_listener" {
  load_balancer_arn = aws_lb.api_gateway_load_balancer.arn
  port              = local.access_port
  protocol          = "HTTP"
  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api_gateway_load_balancer_target_group.arn
  }
}

resource "aws_ecs_task_definition" "api_gateway_ecs_task_definition" {
  family                   = local.api_gateway_name
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = aws_iam_role.iam_role.arn
  container_definitions    = jsonencode([
    {
      name         = local.api_gateway_name
      image        = "${aws_ecr_repository.api_gateway_ecr_repository.repository_url}:latest"
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
          name  = "APPLICATION_RPC_USERS_ADDRESS"
          value = aws_lb.users_load_balancer.dns_name
        },
        {
          name  = "APPLICATION_RPC_USERS_PORT"
          value = tostring(local.container_port)
        },
        {
          name  = "APPLICATION_RPC_ORDERS_WRITE_ADDRESS"
          value = aws_lb.orders_write_side_load_balancer.dns_name
        },
        {
          name  = "APPLICATION_RPC_ORDERS_WRITE_PORT"
          value = tostring(local.container_port)
        },
        {
          name  = "APPLICATION_RPC_ORDERS_READ_ADDRESS"
          value = aws_lb.orders_read_side_load_balancer.dns_name
        },
        {
          name  = "APPLICATION_RPC_ORDERS_READ_PORT"
          value = tostring(local.container_port)
        }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options   = {
          awslogs-group         = aws_cloudwatch_log_group.cloudwatch_log_group.name
          awslogs-region        = var.aws_region
          awslogs-stream-prefix = local.api_gateway_name
        }
      }
    }
  ])
}

resource "aws_ecs_service" "api_gateway_ecs_service" {
  depends_on                         = [aws_lb_listener.api_gateway_application_load_balancer_listener]
  name                               = "${local.api_gateway_name}-service"
  cluster                            = aws_ecs_cluster.ecs_cluster.id
  task_definition                    = aws_ecs_task_definition.api_gateway_ecs_task_definition.arn
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
    target_group_arn = aws_lb_target_group.api_gateway_load_balancer_target_group.arn
    container_name   = local.api_gateway_name
    container_port   = local.container_port
  }
}