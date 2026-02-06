output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "ecr_repository_url" {
  description = "URL of the ECR repository"
  value       = aws_ecr_repository.app.repository_url
}

output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.main.endpoint
}

output "app_url" {
  description = "Base URL of the application"
  value       = "http://${aws_lb.main.dns_name}"
}

output "swagger_url" {
  description = "Swagger UI URL"
  value       = "http://${aws_lb.main.dns_name}/swagger-ui.html"
}

