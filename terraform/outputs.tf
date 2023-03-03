output "api_gateway_ecr_repository_url" {
  value = aws_ecr_repository.api_gateway_ecr_repository.repository_url
}

output "users_ecr_repository_url" {
  value = aws_ecr_repository.users_ecr_repository.repository_url
}

output "orders_write_side_ecr_repository_url" {
  value = aws_ecr_repository.orders_write_side_ecr_repository.repository_url
}

output "orders_read_side_ecr_repository_url" {
  value = aws_ecr_repository.orders_read_side_ecr_repository.repository_url
}

output "api_gateway_dns" {
  value = aws_lb.api_gateway_load_balancer.dns_name
}