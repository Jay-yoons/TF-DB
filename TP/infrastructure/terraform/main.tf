# 하이브리드 클라우드 기반 MSA 인프라 구축
# AWS + Azure + 온프레미스 구성

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

# AWS Provider 설정
provider "aws" {
  region = var.aws_primary_region
  
  default_tags {
    tags = {
      Project     = "Hybrid-Cloud-MSA"
      Environment = var.environment
      Team        = "Multi-Team"
    }
  }
}

# Azure Provider 설정
provider "azurerm" {
  features {}
  subscription_id = var.azure_subscription_id
  tenant_id       = var.azure_tenant_id
}

# AWS EKS 클러스터 (주요 클라우드)
resource "aws_eks_cluster" "primary" {
  name     = "${var.project_name}-eks-primary"
  role_arn = aws_iam_role.eks_cluster.arn
  version  = "1.28"

  vpc_config {
    subnet_ids              = aws_subnet.eks[*].id
    endpoint_private_access = true
    endpoint_public_access  = true
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_cluster_policy,
    aws_iam_role_policy_attachment.eks_vpc_resource_controller,
  ]

  tags = {
    Name = "${var.project_name}-eks-primary"
  }
}

# Azure AKS 클러스터 (보조 클라우드)
resource "azurerm_kubernetes_cluster" "secondary" {
  name                = "${var.project_name}-aks-secondary"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = "${var.project_name}-aks"

  default_node_pool {
    name       = "default"
    node_count = 3
    vm_size    = "Standard_D2s_v3"
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Project     = "Hybrid-Cloud-MSA"
    Environment = var.environment
  }
}

# 멀티리전 구성 - AWS Secondary Region
provider "aws" {
  alias  = "secondary"
  region = var.aws_secondary_region
}

resource "aws_eks_cluster" "secondary" {
  provider = aws.secondary
  name     = "${var.project_name}-eks-secondary"
  role_arn = aws_iam_role.eks_cluster.arn
  version  = "1.28"

  vpc_config {
    subnet_ids              = aws_subnet.eks_secondary[*].id
    endpoint_private_access = true
    endpoint_public_access  = true
  }

  tags = {
    Name = "${var.project_name}-eks-secondary"
  }
}

# API Gateway (AWS)
resource "aws_api_gateway_rest_api" "main" {
  name = "${var.project_name}-api-gateway"
  
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

# Load Balancer (Azure)
resource "azurerm_lb" "main" {
  name                = "${var.project_name}-lb"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  frontend_ip_configuration {
    name                 = "PublicIPAddress"
    public_ip_address_id = azurerm_public_ip.lb.id
  }
}

# 데이터베이스 (멀티리전 구성)
resource "aws_rds_cluster" "primary" {
  cluster_identifier     = "${var.project_name}-aurora-primary"
  engine                = "aurora-postgresql"
  engine_version        = "15.4"
  database_name         = "hybridmsa"
  master_username       = var.db_username
  master_password       = var.db_password
  skip_final_snapshot   = true
  
  availability_zones = ["us-east-1a", "us-east-1b", "us-east-1c"]
}

# Redis Cache (Azure)
resource "azurerm_redis_cache" "main" {
  name                = "${var.project_name}-redis"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  capacity            = 2
  family              = "C"
  sku_name            = "Standard"
  enable_non_ssl_port = false
}

# 모니터링 - Prometheus (AWS)
resource "aws_ecs_cluster" "monitoring" {
  name = "${var.project_name}-monitoring"
  
  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

# 로깅 - CloudWatch + Azure Monitor
resource "aws_cloudwatch_log_group" "main" {
  name              = "/aws/eks/${var.project_name}/cluster"
  retention_in_days = 30
}

# 보안 - VPC, Security Groups
resource "aws_vpc" "main" {
  cidr_block           = "10.0.0.0/16"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.project_name}-vpc"
  }
}

# 변수 정의
variable "project_name" {
  description = "프로젝트 이름"
  type        = string
  default     = "hybrid-cloud-msa"
}

variable "environment" {
  description = "환경 (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "aws_primary_region" {
  description = "AWS 주요 리전"
  type        = string
  default     = "us-east-1"
}

variable "aws_secondary_region" {
  description = "AWS 보조 리전"
  type        = string
  default     = "us-west-2"
}

variable "azure_subscription_id" {
  description = "Azure 구독 ID"
  type        = string
}

variable "azure_tenant_id" {
  description = "Azure 테넌트 ID"
  type        = string
}

variable "db_username" {
  description = "데이터베이스 사용자명"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "데이터베이스 비밀번호"
  type        = string
  sensitive   = true
} 