locals {
  api_gateway_name              = "api-gateway"
  users_name                    = "users"
  orders_write_side_name        = "orders-write-side"
  orders_read_side_name         = "orders-read-side"
  access_port                   = 80
  container_port                = 8080
  health_check_port             = 8081
  api_gateway_health_check_path = "/actuator/health"
  health_check_path             = "/grpc.health.v1.Health/Check"
}