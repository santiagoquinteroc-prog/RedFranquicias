# AWS Infrastructure for Franquicias API

This Terraform stack provisions a complete AWS infrastructure for deploying the Franquicias API using ECS Fargate, RDS MySQL, Application Load Balancer, and ECR.

## Architecture

- **ECS Fargate**: Containerized application running on AWS Fargate
- **RDS MySQL 8.0**: Managed MySQL database instance
- **Application Load Balancer**: Public-facing load balancer for HTTP traffic
- **ECR**: Container registry for Docker images
- **CloudWatch Logs**: Centralized logging for ECS tasks
- **Security Groups**: Network security for ALB, ECS, and RDS

## Prerequisites

- AWS account with appropriate IAM permissions for:
  - ECS (cluster, service, task definition)
  - ECR (repository, image push/pull)
  - RDS (instance creation and management)
  - ALB (load balancer, target groups, listeners)
  - CloudWatch Logs (log group creation and writing)
  - IAM (role and policy creation)
  - VPC and networking (security groups, subnets)
- AWS CLI installed and configured (`aws configure`)
- Terraform >= 1.5 installed
- Docker installed and running locally

## Setup

1. Build the application JAR file:
   ```bash
   ./gradlew clean build
   ```

2. Navigate to the Terraform directory:
   ```bash
   cd infra/terraform/aws-franquicias-full
   ```

3. Copy the example variables file:
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   ```

4. Edit `terraform.tfvars` and set your values:
   - `db_username`: Database master username
   - `db_password`: Secure database password (will be marked as sensitive)
   - Adjust other variables as needed (region, project_name, etc.)

## Terraform Commands

### Initialize Terraform
```bash
terraform init
```

### Plan Changes
```bash
terraform plan
```

### Apply Infrastructure
```bash
terraform apply
```

After applying, Terraform will output:
- `alb_dns_name`: DNS name of the load balancer
- `ecr_repository_url`: ECR repository URL for pushing images
- `rds_endpoint`: RDS database endpoint
- `app_url`: Base URL of the application
- `swagger_url`: Swagger UI URL

### Destroy Infrastructure
```bash
terraform destroy
```

**Warning**: This will delete all resources including the RDS database. Make sure you have backups if needed.

## Deployment Order

Follow these steps in order to deploy the application:

### Step 0: Build Application and Navigate to Terraform

1. Build the application JAR file from the project root:
   ```bash
   ./gradlew clean build
   ```

2. Navigate to the Terraform directory:
   ```bash
   cd infra/terraform/aws-franquicias-full
   ```

### Step 1: Create Infrastructure

Run Terraform to create all AWS resources (ECR repository, RDS database, ECS cluster, ALB):

```bash
terraform init
terraform apply
```

This creates:
- ECR repository (for storing Docker images)
- RDS MySQL instance
- ECS cluster and service
- Application Load Balancer

**Note**: The ECS service will initially fail health checks because no Docker image exists in ECR yet. This is expected.

After `terraform apply` completes, note the `ecr_repository_url` from the output:
```bash
terraform output ecr_repository_url
```

### Step 2: Build and Push Docker Image

1. **Navigate back to project root** (if you're still in the terraform directory):
   ```bash
   cd ../../..
   ```

2. **Authenticate Docker with ECR**:
   ```bash
   aws ecr get-login-password --region <aws_region> | docker login --username AWS --password-stdin <ecr_repository_url>
   ```
   
   Replace `<aws_region>` with your region (e.g., `us-east-2`) and `<ecr_repository_url>` with the output from Step 1 (run `terraform output ecr_repository_url` from the terraform directory).

3. **Build the Docker image**:
   ```bash
   docker build -t <ecr_repository_url>:<image_tag> .
   ```
   
   Example:
   ```bash
   docker build -t 123456789012.dkr.ecr.us-east-2.amazonaws.com/red-franquicias:latest .
   ```

4. **Push the image to ECR**:
   ```bash
   docker push <ecr_repository_url>:<image_tag>
   ```

### Step 3: Deploy Application

After pushing the image, force ECS to deploy:

**Option A: Force New Deployment (Same Image Tag)**

If using the same tag (e.g., `latest`), force ECS to pull the new image:

```bash
aws ecs update-service \
  --cluster <cluster_name> \
  --service <service_name> \
  --force-new-deployment \
  --region <aws_region>
```

Or via AWS Console: ECS → Clusters → Your cluster → Services → Your service → Update → Check "Force new deployment" → Update

**Option B: Update Image Tag via Terraform**

1. Update `image_tag` in `terraform.tfvars` (e.g., `image_tag = "v1.0.1"`)
2. Build and push the image with the new tag:
   ```bash
   docker build -t <ecr_repository_url>:v1.0.1 .
   docker push <ecr_repository_url>:v1.0.1
   ```
3. Apply Terraform changes:
   ```bash
   terraform apply
   ```

This creates a new task definition revision and triggers a rolling deployment.

## Updating the Application

For subsequent deployments:

1. Build and push a new Docker image to ECR (with same or new tag)
2. Force new ECS deployment (Option A) or update `image_tag` and run `terraform apply` (Option B)

## Accessing the Application

After deployment, access the application using the Terraform outputs:

- **Base URL**: Use `terraform output app_url` or visit `http://<alb_dns_name>`
- **Swagger UI**: Use `terraform output swagger_url` or visit `http://<alb_dns_name>/swagger-ui.html`
- **Health Check**: `http://<alb_dns_name>/health`

The ALB health check is configured to use `/health` endpoint. Ensure your application is healthy before accessing other endpoints.

## Environment Variables

The ECS task definition sets the following environment variables for Spring Boot:

- `DB_HOST`: RDS endpoint (automatically set)
- `DB_PORT`: `3306`
- `DB_NAME`: Database name from `var.db_name`
- `DB_USERNAME`: Database username from `var.db_username`
- `DB_PASSWORD`: Database password from `var.db_password`
- `SPRING_PROFILES_ACTIVE`: `prod`

These match the Spring Boot configuration in `application.yaml` which uses environment variables with defaults.

## Cost Considerations

This infrastructure includes:

- **ECS Fargate**: Charged per vCPU and memory used, per hour
- **RDS MySQL**: Charged per instance hour (db.t3.micro is the smallest)
- **ALB**: Charged per hour and per LCU (Load Balancer Capacity Unit)
- **ECR**: Charged per GB/month for stored images
- **CloudWatch Logs**: Charged per GB ingested and stored

**Estimated monthly cost** (approximate, varies by region and usage):
- ECS Fargate (1 task, 0.5 vCPU, 1GB): ~$15-20/month
- RDS db.t3.micro: ~$15-20/month
- ALB: ~$20-25/month
- ECR: ~$0.10/GB/month (minimal for small images)
- CloudWatch Logs: ~$0.50/GB ingested

**Total**: Approximately $50-70/month for minimal usage.

**Important**: Always run `terraform destroy` when you're done testing to avoid ongoing charges.

## Cleanup

To completely remove all resources:

```bash
terraform destroy
```

This will:
- Delete the ECS service and tasks
- Delete the ECS cluster
- Delete the ALB and target groups
- Delete the RDS instance (with `skip_final_snapshot = true`, no snapshot will be created)
- Delete the ECR repository (images will be deleted)
- Delete CloudWatch log groups
- Delete security groups
- Delete IAM roles

**Note**: The RDS instance is configured with `skip_final_snapshot = true` and `deletion_protection = false` for easy cleanup. If you need to preserve data, modify these settings before destroying.

## Troubleshooting

### ECS Tasks Not Starting

1. Check CloudWatch Logs: `/ecs/<project_name>`
2. Verify the Docker image exists in ECR with the correct tag
3. Check security group rules allow traffic between ALB and ECS
4. Verify RDS security group allows connections from ECS security group

### Health Check Failures

1. Ensure `/health` endpoint returns HTTP 200
2. Check container logs in CloudWatch
3. Verify the container is listening on port 8080
4. Check security group allows traffic from ALB to ECS on port 8080

### Database Connection Issues

1. Verify RDS endpoint is correct in environment variables
2. Check RDS security group allows MySQL (port 3306) from ECS security group
3. Verify database credentials are correct
4. Check RDS instance is in "available" state

### Image Push Failures

1. Ensure you're authenticated: `aws ecr get-login-password`
2. Verify the ECR repository exists
3. Check IAM permissions for ECR push
4. Ensure Docker is running locally

