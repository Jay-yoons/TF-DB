#!/bin/bash

# AWS ECS 배포 스크립트
# Oracle DB와 함께 User Service를 AWS ECS에 배포

set -e

# 환경 변수 설정
ECR_REPOSITORY_NAME="user-service"
ECS_CLUSTER_NAME="fog-cluster"
ECS_SERVICE_NAME="user-service"
ECS_TASK_DEFINITION_NAME="user-service-task"
AWS_REGION="ap-northeast-2"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 로그 함수
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# AWS CLI 설치 확인
check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI가 설치되지 않았습니다."
        exit 1
    fi
    log_info "AWS CLI 확인 완료"
}

# Docker 설치 확인
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker가 설치되지 않았습니다."
        exit 1
    fi
    log_info "Docker 확인 완료"
}

# ECR 리포지토리 생성
create_ecr_repository() {
    log_info "ECR 리포지토리 생성 중..."
    
    if aws ecr describe-repositories --repository-names $ECR_REPOSITORY_NAME --region $AWS_REGION &> /dev/null; then
        log_warn "ECR 리포지토리가 이미 존재합니다: $ECR_REPOSITORY_NAME"
    else
        aws ecr create-repository --repository-name $ECR_REPOSITORY_NAME --region $AWS_REGION
        log_info "ECR 리포지토리 생성 완료: $ECR_REPOSITORY_NAME"
    fi
}

# Docker 이미지 빌드 및 푸시
build_and_push_image() {
    log_info "Docker 이미지 빌드 중..."
    
    # ECR 로그인
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $(aws sts get-caller-identity --query Account --output text).dkr.ecr.$AWS_REGION.amazonaws.com
    
    # ECR 리포지토리 URI 가져오기
    ECR_REPOSITORY_URI=$(aws ecr describe-repositories --repository-names $ECR_REPOSITORY_NAME --region $AWS_REGION --query 'repositories[0].repositoryUri' --output text)
    
    # 이미지 빌드
    docker build -t $ECR_REPOSITORY_NAME .
    
    # 이미지 태그
    docker tag $ECR_REPOSITORY_NAME:latest $ECR_REPOSITORY_URI:latest
    
    # 이미지 푸시
    log_info "Docker 이미지 푸시 중..."
    docker push $ECR_REPOSITORY_URI:latest
    
    log_info "Docker 이미지 빌드 및 푸시 완료"
}

# ECS 클러스터 생성
create_ecs_cluster() {
    log_info "ECS 클러스터 생성 중..."
    
    if aws ecs describe-clusters --clusters $ECS_CLUSTER_NAME --region $AWS_REGION --query 'clusters[0].status' --output text &> /dev/null; then
        log_warn "ECS 클러스터가 이미 존재합니다: $ECS_CLUSTER_NAME"
    else
        aws ecs create-cluster --cluster-name $ECS_CLUSTER_NAME --region $AWS_REGION
        log_info "ECS 클러스터 생성 완료: $ECS_CLUSTER_NAME"
    fi
}

# ECS 태스크 정의 생성
create_task_definition() {
    log_info "ECS 태스크 정의 생성 중..."
    
    # ECR 리포지토리 URI 가져오기
    ECR_REPOSITORY_URI=$(aws ecr describe-repositories --repository-names $ECR_REPOSITORY_NAME --region $AWS_REGION --query 'repositories[0].repositoryUri' --output text)
    
    # 태스크 정의 JSON 생성
    cat > task-definition.json << EOF
{
    "family": "$ECS_TASK_DEFINITION_NAME",
    "networkMode": "awsvpc",
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "512",
    "memory": "1024",
    "executionRoleArn": "arn:aws:iam::$(aws sts get-caller-identity --query Account --output text):role/ecsTaskExecutionRole",
    "containerDefinitions": [
        {
            "name": "user-service",
            "image": "$ECR_REPOSITORY_URI:latest",
            "portMappings": [
                {
                    "containerPort": 8080,
                    "protocol": "tcp"
                }
            ],
            "environment": [
                {
                    "name": "SPRING_PROFILES_ACTIVE",
                    "value": "prod"
                },
                {
                    "name": "DB_HOST",
                    "value": "\${DB_HOST}"
                },
                {
                    "name": "DB_PORT",
                    "value": "\${DB_PORT}"
                },
                {
                    "name": "DB_SID",
                    "value": "\${DB_SID}"
                },
                {
                    "name": "DB_USERNAME",
                    "value": "\${DB_USERNAME}"
                },
                {
                    "name": "DB_PASSWORD",
                    "value": "\${DB_PASSWORD}"
                }
            ],
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": "/ecs/user-service",
                    "awslogs-region": "$AWS_REGION",
                    "awslogs-stream-prefix": "ecs"
                }
            },
            "healthCheck": {
                "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
                "interval": 30,
                "timeout": 5,
                "retries": 3,
                "startPeriod": 60
            }
        }
    ]
}
EOF
    
    # 태스크 정의 등록
    aws ecs register-task-definition --cli-input-json file://task-definition.json --region $AWS_REGION
    
    log_info "ECS 태스크 정의 생성 완료"
}

# ECS 서비스 생성
create_ecs_service() {
    log_info "ECS 서비스 생성 중..."
    
    # 서브넷 ID 가져오기 (기본 VPC 사용)
    SUBNET_IDS=$(aws ec2 describe-subnets --region $AWS_REGION --filters "Name=default-for-az,Values=true" --query 'Subnets[*].SubnetId' --output text | tr '\t' ',')
    
    # 보안 그룹 생성
    SECURITY_GROUP_ID=$(aws ec2 create-security-group --group-name user-service-sg --description "Security group for user service" --region $AWS_REGION --query 'GroupId' --output text)
    
    # 인바운드 규칙 추가
    aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 8080 --cidr 0.0.0.0/0 --region $AWS_REGION
    
    # 서비스 생성
    aws ecs create-service \
        --cluster $ECS_CLUSTER_NAME \
        --service-name $ECS_SERVICE_NAME \
        --task-definition $ECS_TASK_DEFINITION_NAME \
        --desired-count 1 \
        --launch-type FARGATE \
        --network-configuration "awsvpcConfiguration={subnets=[$(echo $SUBNET_IDS | cut -d',' -f1)],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
        --region $AWS_REGION
    
    log_info "ECS 서비스 생성 완료"
}

# CloudWatch 로그 그룹 생성
create_log_group() {
    log_info "CloudWatch 로그 그룹 생성 중..."
    
    if aws logs describe-log-groups --log-group-name-prefix "/ecs/user-service" --region $AWS_REGION --query 'logGroups[0].logGroupName' --output text | grep -q "/ecs/user-service"; then
        log_warn "CloudWatch 로그 그룹이 이미 존재합니다: /ecs/user-service"
    else
        aws logs create-log-group --log-group-name "/ecs/user-service" --region $AWS_REGION
        log_info "CloudWatch 로그 그룹 생성 완료: /ecs/user-service"
    fi
}

# 메인 함수
main() {
    log_info "AWS ECS 배포 시작"
    
    check_aws_cli
    check_docker
    create_ecr_repository
    build_and_push_image
    create_ecs_cluster
    create_log_group
    create_task_definition
    create_ecs_service
    
    log_info "AWS ECS 배포 완료!"
    log_info "서비스 URL: http://$(aws ecs describe-services --cluster $ECS_CLUSTER_NAME --services $ECS_SERVICE_NAME --region $AWS_REGION --query 'services[0].loadBalancers[0].hostname' --output text)"
}

# 스크립트 실행
main "$@"
