#!/bin/bash

# 🕐 AWS ECS 한국시간대 설정 스크립트
# AWS CLI를 사용하여 ECS 서비스의 환경변수를 직접 업데이트

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# 환경 변수 설정
AWS_REGION="ap-northeast-2"
CLUSTER_NAME="team-fog-cluster"
USER_SERVICE_NAME="team-fog-user-service"
STORE_SERVICE_NAME="team-fog-store-service"
RESERVATION_SERVICE_NAME="team-fog-reservation-service"

# 1. User Service 시간대 설정
update_user_service_timezone() {
    log_info "User Service 시간대 설정 업데이트 중..."
    
    # 현재 태스크 정의 가져오기
    CURRENT_TASK_DEF=$(aws ecs describe-task-definition \
        --task-definition user-service \
        --region $AWS_REGION \
        --query 'taskDefinition' \
        --output json)
    
    # 환경변수 추가/업데이트
    UPDATED_TASK_DEF=$(echo $CURRENT_TASK_DEF | jq '
        .containerDefinitions[0].environment += [
            {"name": "TZ", "value": "Asia/Seoul"},
            {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"}
        ]
    ')
    
    # 새 태스크 정의 등록
    NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
        --cli-input-json "$UPDATED_TASK_DEF" \
        --region $AWS_REGION \
        --query 'taskDefinition.taskDefinitionArn' \
        --output text)
    
    # 서비스 업데이트
    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $USER_SERVICE_NAME \
        --task-definition $NEW_TASK_DEF_ARN \
        --region $AWS_REGION
    
    log_success "User Service 시간대 설정 완료"
}

# 2. Store Service 시간대 설정
update_store_service_timezone() {
    log_info "Store Service 시간대 설정 업데이트 중..."
    
    CURRENT_TASK_DEF=$(aws ecs describe-task-definition \
        --task-definition store-service-task \
        --region $AWS_REGION \
        --query 'taskDefinition' \
        --output json)
    
    UPDATED_TASK_DEF=$(echo $CURRENT_TASK_DEF | jq '
        .containerDefinitions[0].environment += [
            {"name": "TZ", "value": "Asia/Seoul"},
            {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"}
        ]
    ')
    
    NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
        --cli-input-json "$UPDATED_TASK_DEF" \
        --region $AWS_REGION \
        --query 'taskDefinition.taskDefinitionArn' \
        --output text)
    
    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $STORE_SERVICE_NAME \
        --task-definition $NEW_TASK_DEF_ARN \
        --region $AWS_REGION
    
    log_success "Store Service 시간대 설정 완료"
}

# 3. Reservation Service 시간대 설정
update_reservation_service_timezone() {
    log_info "Reservation Service 시간대 설정 업데이트 중..."
    
    CURRENT_TASK_DEF=$(aws ecs describe-task-definition \
        --task-definition reservation-service-task \
        --region $AWS_REGION \
        --query 'taskDefinition' \
        --output json)
    
    UPDATED_TASK_DEF=$(echo $CURRENT_TASK_DEF | jq '
        .containerDefinitions[0].environment += [
            {"name": "TZ", "value": "Asia/Seoul"},
            {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"}
        ]
    ')
    
    NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
        --cli-input-json "$UPDATED_TASK_DEF" \
        --region $AWS_REGION \
        --query 'taskDefinition.taskDefinitionArn' \
        --output text)
    
    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $RESERVATION_SERVICE_NAME \
        --task-definition $NEW_TASK_DEF_ARN \
        --region $AWS_REGION
    
    log_success "Reservation Service 시간대 설정 완료"
}

# 4. 시간대 설정 확인
verify_timezone_settings() {
    log_info "시간대 설정 확인 중..."
    
    # 각 서비스의 현재 태스크 확인
    SERVICES=($USER_SERVICE_NAME $STORE_SERVICE_NAME $RESERVATION_SERVICE_NAME)
    
    for SERVICE in "${SERVICES[@]}"; do
        log_info "서비스: $SERVICE"
        
        # 현재 실행 중인 태스크 확인
        TASK_ARN=$(aws ecs list-tasks \
            --cluster $CLUSTER_NAME \
            --service-name $SERVICE \
            --region $AWS_REGION \
            --query 'taskArns[0]' \
            --output text)
        
        if [ "$TASK_ARN" != "None" ]; then
            # 태스크의 환경변수 확인
            aws ecs describe-tasks \
                --cluster $CLUSTER_NAME \
                --tasks $TASK_ARN \
                --region $AWS_REGION \
                --query 'tasks[0].overrides.containerOverrides[0].environment[?name==`TZ` || name==`JAVA_OPTS`]' \
                --output table
        else
            log_warning "서비스 $SERVICE에 실행 중인 태스크가 없습니다."
        fi
    done
}

# 5. 로그에서 시간대 확인
check_logs_timezone() {
    log_info "로그에서 시간대 확인 중..."
    
    # 최근 로그 확인 (각 서비스별)
    SERVICES=($USER_SERVICE_NAME $STORE_SERVICE_NAME $RESERVATION_SERVICE_NAME)
    
    for SERVICE in "${SERVICES[@]}"; do
        log_info "서비스: $SERVICE 로그 확인"
        
        # CloudWatch 로그에서 시간대 관련 로그 확인
        aws logs filter-log-events \
            --log-group-name "/ecs/$SERVICE" \
            --region $AWS_REGION \
            --start-time $(date -d '1 hour ago' +%s)000 \
            --filter-pattern "timezone OR time OR Asia/Seoul" \
            --query 'events[*].message' \
            --output text | head -5
    done
}

# 메인 실행 함수
main() {
    log_info "AWS ECS 한국시간대 설정 시작"
    
    # AWS CLI 확인
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI가 설치되지 않았습니다."
        exit 1
    fi
    
    # jq 확인
    if ! command -v jq &> /dev/null; then
        log_error "jq가 설치되지 않았습니다."
        log_info "Ubuntu/Debian: sudo apt-get install jq"
        log_info "CentOS/RHEL: sudo yum install jq"
        exit 1
    fi
    
    # 각 서비스 시간대 설정 업데이트
    update_user_service_timezone
    update_store_service_timezone
    update_reservation_service_timezone
    
    # 설정 확인
    verify_timezone_settings
    check_logs_timezone
    
    log_success "모든 서비스의 한국시간대 설정 완료"
    log_info "설정된 환경변수:"
    log_info "- TZ=Asia/Seoul"
    log_info "- JAVA_OPTS=-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"
}

# 스크립트 실행
main "$@"
