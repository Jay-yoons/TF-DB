# 🏗️ AWS ECS MSA 인프라 설정 가이드

## 📋 개요

Team-FOG 프로젝트의 MSA 서비스들을 AWS ECS에 배포하기 위한 인프라 설정 가이드입니다.

## 🔧 1단계: VPC 및 네트워크 설정

### 1.1 VPC 생성

```bash
# VPC 생성
aws ec2 create-vpc \
    --cidr-block 10.0.0.0/16 \
    --tag-specifications ResourceType=vpc,Tags=[{Key=Name,Value=team-fog-vpc}]

# VPC ID 저장
export VPC_ID=$(aws ec2 describe-vpcs --filters "Name=tag:Name,Values=team-fog-vpc" --query 'Vpcs[0].VpcId' --output text)
```

### 1.2 서브넷 생성

```bash
# 퍼블릭 서브넷 생성 (ALB용)
aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.1.0/24 \
    --availability-zone ap-northeast-2a \
    --tag-specifications ResourceType=subnet,Tags=[{Key=Name,Value=team-fog-public-subnet-1}]

aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.2.0/24 \
    --availability-zone ap-northeast-2c \
    --tag-specifications ResourceType=subnet,Tags=[{Key=Name,Value=team-fog-public-subnet-2}]

# 프라이빗 서브넷 생성 (ECS 서비스용)
aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.3.0/24 \
    --availability-zone ap-northeast-2a \
    --tag-specifications ResourceType=subnet,Tags=[{Key=Name,Value=team-fog-private-subnet-1}]

aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.4.0/24 \
    --availability-zone ap-northeast-2c \
    --tag-specifications ResourceType=subnet,Tags=[{Key=Name,Value=team-fog-private-subnet-2}]
```

### 1.3 인터넷 게이트웨이 및 NAT 게이트웨이

```bash
# 인터넷 게이트웨이 생성
aws ec2 create-internet-gateway \
    --tag-specifications ResourceType=internet-gateway,Tags=[{Key=Name,Value=team-fog-igw}]

# VPC에 인터넷 게이트웨이 연결
aws ec2 attach-internet-gateway \
    --vpc-id $VPC_ID \
    --internet-gateway-id $IGW_ID

# NAT 게이트웨이용 Elastic IP 할당
aws ec2 allocate-address \
    --domain vpc \
    --tag-specifications ResourceType=elastic-ip,Tags=[{Key=Name,Value=team-fog-nat-eip}]

# NAT 게이트웨이 생성
aws ec2 create-nat-gateway \
    --subnet-id $PUBLIC_SUBNET_1_ID \
    --allocation-id $EIP_ALLOCATION_ID \
    --tag-specifications ResourceType=natgateway,Tags=[{Key=Name,Value=team-fog-nat-gateway}]
```

## 🔧 2단계: 보안 그룹 설정

### 2.1 ALB 보안 그룹

```bash
# ALB 보안 그룹 생성
aws ec2 create-security-group \
    --group-name team-fog-alb-sg \
    --description "Security group for Application Load Balancer" \
    --vpc-id $VPC_ID

# ALB 보안 그룹 규칙 설정
aws ec2 authorize-security-group-ingress \
    --group-id $ALB_SG_ID \
    --protocol tcp \
    --port 80 \
    --cidr 0.0.0.0/0

aws ec2 authorize-security-group-ingress \
    --group-id $ALB_SG_ID \
    --protocol tcp \
    --port 443 \
    --cidr 0.0.0.0/0
```

### 2.2 ECS 서비스 보안 그룹

```bash
# ECS 서비스 보안 그룹 생성
aws ec2 create-security-group \
    --group-name team-fog-ecs-sg \
    --description "Security group for ECS services" \
    --vpc-id $VPC_ID

# ECS 서비스 보안 그룹 규칙 설정
aws ec2 authorize-security-group-ingress \
    --group-id $ECS_SG_ID \
    --protocol tcp \
    --port 8080-8082 \
    --source-group $ALB_SG_ID

# Oracle DB 보안 그룹
aws ec2 create-security-group \
    --group-name team-fog-oracle-db-sg \
    --description "Security group for Oracle Database" \
    --vpc-id $VPC_ID

aws ec2 authorize-security-group-ingress \
    --group-id $ORACLE_DB_SG_ID \
    --protocol tcp \
    --port 1521 \
    --source-group $ECS_SG_ID
```

## 🔧 3단계: ECS 클러스터 생성

```bash
# ECS 클러스터 생성
aws ecs create-cluster \
    --cluster-name team-fog-cluster \
    --capacity-providers FARGATE \
    --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1 \
    --tags key=Project,value=Team-FOG
```

## 🔧 4단계: ECR 리포지토리 생성

```bash
# 각 서비스별 ECR 리포지토리 생성
aws ecr create-repository \
    --repository-name team-fog-user-service \
    --image-scanning-configuration scanOnPush=true

aws ecr create-repository \
    --repository-name team-fog-store-service \
    --image-scanning-configuration scanOnPush=true

aws ecr create-repository \
    --repository-name team-fog-reservation-service \
    --image-scanning-configuration scanOnPush=true
```

## 🔧 5단계: IAM 역할 생성

### 5.1 ECS Task Execution Role

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            ],
            "Resource": "*"
        }
    ]
}
```

### 5.2 ECS Task Role

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue",
                "ssm:GetParameters",
                "ssm:GetParameter"
            ],
            "Resource": "*"
        }
    ]
}
```

## 🔧 6단계: Secrets Manager 설정

```bash
# Oracle DB 비밀번호 저장
aws secretsmanager create-secret \
    --name "team-fog/oracle/primarydb/password" \
    --description "Oracle PrimaryDB password" \
    --secret-string "{\"password\":\"your-secure-password\"}"

aws secretsmanager create-secret \
    --name "team-fog/oracle/standbydb/password" \
    --description "Oracle StandbyDB password" \
    --secret-string "{\"password\":\"your-secure-password\"}"

# AWS Cognito 설정 저장
aws secretsmanager create-secret \
    --name "team-fog/cognito/client-secret" \
    --description "AWS Cognito client secret" \
    --secret-string "{\"client-secret\":\"your-cognito-client-secret\"}"
```

## 📋 체크리스트

### 인프라 설정 완료 확인사항

- [ ] VPC 및 서브넷 생성
- [ ] 인터넷 게이트웨이 및 NAT 게이트웨이 설정
- [ ] 보안 그룹 설정
- [ ] ECS 클러스터 생성
- [ ] ECR 리포지토리 생성
- [ ] IAM 역할 생성
- [ ] Secrets Manager 설정

### 다음 단계

1. **Oracle DB ECS 서비스 배포**
2. **각 마이크로서비스 배포**
3. **Application Load Balancer 설정**
4. **도메인 및 SSL 인증서 설정**

## 📞 지원

문제가 발생하면 다음 연락처로 문의하세요:
- **DevOps 담당자**: [담당자명]
- **AWS 관리자**: [담당자명]
