#!/bin/bash

# 🚀 Team-FOG CloudFormation 배포 스크립트
# AWS CloudFormation을 사용하여 전체 인프라를 한번에 구축

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 로그 함수
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 환경변수 설정
STACK_NAME="team-fog-infrastructure"
TEMPLATE_FILE="11_CLOUDFORMATION_TEMPLATE.yml"
AWS_REGION="ap-northeast-2"

# 환경 확인
check_environment() {
    log_info "환경 설정 확인 중..."
    
    # AWS CLI 확인
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI가 설치되지 않았습니다."
        exit 1
    fi
    
    # AWS 계정 확인
    AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    if [ -z "$AWS_ACCOUNT_ID" ]; then
        log_error "AWS 계정 ID를 가져올 수 없습니다."
        exit 1
    fi
    
    # 템플릿 파일 확인
    if [ ! -f "$TEMPLATE_FILE" ]; then
        log_error "CloudFormation 템플릿 파일을 찾을 수 없습니다: $TEMPLATE_FILE"
        exit 1
    fi
    
    log_success "환경 설정 확인 완료"
    log_info "AWS 계정 ID: $AWS_ACCOUNT_ID"
    log_info "AWS 리전: $AWS_REGION"
    log_info "스택 이름: $STACK_NAME"
}

# CloudFormation 스택 생성
create_stack() {
    log_info "CloudFormation 스택 생성 중..."
    
    # 필수 파라미터 확인
    if [ -z "$ORACLE_DB_HOST" ]; then
        log_error "ORACLE_DB_HOST 환경변수가 설정되지 않았습니다."
        log_info "사용법: ORACLE_DB_HOST=10.0.x.x ./12_CLOUDFORMATION_DEPLOY.sh"
        exit 1
    fi
    
    if [ -z "$ORACLE_DB_PASSWORD" ]; then
        log_error "ORACLE_DB_PASSWORD 환경변수가 설정되지 않았습니다."
        log_info "사용법: ORACLE_DB_PASSWORD=your_password ./12_CLOUDFORMATION_DEPLOY.sh"
        exit 1
    fi
    
    # CloudFormation 스택 생성
    aws cloudformation create-stack \
        --stack-name $STACK_NAME \
        --template-body file://$TEMPLATE_FILE \
        --parameters \
            ParameterKey=ProjectName,ParameterValue=team-fog \
            ParameterKey=Environment,ParameterValue=prod \
            ParameterKey=VpcCidr,ParameterValue=10.0.0.0/16 \
            ParameterKey=PublicSubnet1Cidr,ParameterValue=10.0.1.0/24 \
            ParameterKey=PublicSubnet2Cidr,ParameterValue=10.0.2.0/24 \
            ParameterKey=PrivateSubnet1Cidr,ParameterValue=10.0.3.0/24 \
            ParameterKey=PrivateSubnet2Cidr,ParameterValue=10.0.4.0/24 \
            ParameterKey=OracleDbHost,ParameterValue=$ORACLE_DB_HOST \
            ParameterKey=OracleDbPort,ParameterValue=1521 \
            ParameterKey=OracleDbSid,ParameterValue=ORCL \
            ParameterKey=OracleDbUsername,ParameterValue=teamfog_user \
            ParameterKey=OracleDbPassword,ParameterValue=$ORACLE_DB_PASSWORD \
        --capabilities CAPABILITY_NAMED_IAM \
        --region $AWS_REGION
    
    log_success "CloudFormation 스택 생성 요청 완료"
    log_info "스택 생성 상태를 확인하려면: aws cloudformation describe-stacks --stack-name $STACK_NAME"
}

# 스택 상태 확인
check_stack_status() {
    log_info "스택 생성 상태 확인 중..."
    
    while true; do
        STATUS=$(aws cloudformation describe-stacks \
            --stack-name $STACK_NAME \
            --query 'Stacks[0].StackStatus' \
            --output text \
            --region $AWS_REGION 2>/dev/null || echo "STACK_NOT_FOUND")
        
        case $STATUS in
            "CREATE_COMPLETE")
                log_success "스택 생성 완료!"
                break
                ;;
            "CREATE_IN_PROGRESS")
                log_info "스택 생성 중... (현재 상태: $STATUS)"
                sleep 30
                ;;
            "ROLLBACK_COMPLETE"|"ROLLBACK_IN_PROGRESS"|"CREATE_FAILED")
                log_error "스택 생성 실패! (상태: $STATUS)"
                log_info "실패 원인을 확인하려면: aws cloudformation describe-stack-events --stack-name $STACK_NAME"
                exit 1
                ;;
            *)
                log_warning "알 수 없는 상태: $STATUS"
                sleep 30
                ;;
        esac
    done
}

# 출력값 확인
show_outputs() {
    log_info "생성된 리소스 정보:"
    
    # VPC ID
    VPC_ID=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`VPCId`].OutputValue' \
        --output text \
        --region $AWS_REGION)
    log_info "VPC ID: $VPC_ID"
    
    # ECS 클러스터 이름
    ECS_CLUSTER=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`ECSClusterName`].OutputValue' \
        --output text \
        --region $AWS_REGION)
    log_info "ECS 클러스터: $ECS_CLUSTER"
    
    # ECR 저장소 URI
    ECR_URI=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`UserServiceECRRepository`].OutputValue' \
        --output text \
        --region $AWS_REGION)
    log_info "ECR 저장소: $ECR_URI"
    
    # Cognito User Pool ID
    COGNITO_USER_POOL=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`CognitoUserPoolId`].OutputValue' \
        --output text \
        --region $AWS_REGION)
    log_info "Cognito User Pool ID: $COGNITO_USER_POOL"
    
    # ALB DNS 이름
    ALB_DNS=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`ApplicationLoadBalancerDNS`].OutputValue' \
        --output text \
        --region $AWS_REGION)
    log_info "Application Load Balancer DNS: $ALB_DNS"
    
    # User Service URL
    USER_SERVICE_URL=$(aws cloudformation describe-stacks \
        --stack-name $STACK_NAME \
        --query 'Stacks[0].Outputs[?OutputKey==`UserServiceURL`].OutputValue' \
        --output text \
        --region $AWS_REGION)
    log_info "User Service URL: $USER_SERVICE_URL"
}

# 스택 삭제 (필요시)
delete_stack() {
    log_warning "스택 삭제를 시작합니다..."
    
    aws cloudformation delete-stack \
        --stack-name $STACK_NAME \
        --region $AWS_REGION
    
    log_info "스택 삭제 요청 완료"
    log_info "삭제 상태 확인: aws cloudformation describe-stacks --stack-name $STACK_NAME"
}

# 메인 실행 함수
main() {
    log_info "🚀 Team-FOG CloudFormation 배포 시작"
    
    case "${1:-create}" in
        "create")
            check_environment
            create_stack
            check_stack_status
            show_outputs
            log_success "✅ CloudFormation 배포 완료!"
            ;;
        "delete")
            delete_stack
            ;;
        "status")
            check_stack_status
            ;;
        "outputs")
            show_outputs
            ;;
        *)
            log_error "잘못된 명령어입니다."
            log_info "사용법: $0 [create|delete|status|outputs]"
            log_info "예시: ORACLE_DB_HOST=10.0.x.x ORACLE_DB_PASSWORD=your_password $0 create"
            exit 1
            ;;
    esac
}

# 스크립트 실행
main "$@"
