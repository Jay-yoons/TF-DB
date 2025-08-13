#!/bin/bash

# 🚀 예약 몰림 상황 대비 인프라 설정 스크립트
# 자동 스케일링, 로드 밸런싱, 캐싱 등을 설정

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
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
CLUSTER_NAME="team-fog-cluster"
SERVICE_NAME="team-fog-reservation-service"

# 1. 자동 스케일링 설정
setup_auto_scaling() {
    log_info "자동 스케일링 설정 시작..."
    
    # 스케일링 대상 등록
    aws application-autoscaling register-scalable-target \
        --service-namespace ecs \
        --scalable-dimension ecs:service:DesiredCount \
        --resource-id service/$CLUSTER_NAME/$SERVICE_NAME \
        --min-capacity 2 \
        --max-capacity 20 \
        --role-arn arn:aws:iam::$AWS_ACCOUNT_ID:role/ecsAutoscaleRole
    
    # CPU 기반 스케일링 정책
    aws application-autoscaling put-scaling-policy \
        --service-namespace ecs \
        --scalable-dimension ecs:service:DesiredCount \
        --resource-id service/$CLUSTER_NAME/$SERVICE_NAME \
        --policy-name cpu-scaling-policy \
        --policy-type TargetTrackingScaling \
        --target-tracking-scaling-policy-configuration '{
            "TargetValue": 70.0,
            "PredefinedMetricSpecification": {
                "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
            },
            "ScaleOutCooldown": 60,
            "ScaleInCooldown": 300
        }'
    
    # 메모리 기반 스케일링 정책
    aws application-autoscaling put-scaling-policy \
        --service-namespace ecs \
        --scalable-dimension ecs:service:DesiredCount \
        --resource-id service/$CLUSTER_NAME/$SERVICE_NAME \
        --policy-name memory-scaling-policy \
        --policy-type TargetTrackingScaling \
        --target-tracking-scaling-policy-configuration '{
            "TargetValue": 80.0,
            "PredefinedMetricSpecification": {
                "PredefinedMetricType": "ECSServiceAverageMemoryUtilization"
            },
            "ScaleOutCooldown": 60,
            "ScaleInCooldown": 300
        }'
    
    log_success "자동 스케일링 설정 완료"
}

# 2. CloudWatch 알람 설정
setup_cloudwatch_alarms() {
    log_info "CloudWatch 알람 설정 시작..."
    
    # CPU 사용률 알람
    aws cloudwatch put-metric-alarm \
        --alarm-name "reservation-service-high-cpu" \
        --alarm-description "Reservation Service CPU 사용률이 높음" \
        --metric-name CPUUtilization \
        --namespace AWS/ECS \
        --statistic Average \
        --period 60 \
        --threshold 80 \
        --comparison-operator GreaterThanThreshold \
        --evaluation-periods 2 \
        --dimensions Name=ServiceName,Value=$SERVICE_NAME Name=ClusterName,Value=$CLUSTER_NAME \
        --alarm-actions arn:aws:sns:ap-northeast-2:$AWS_ACCOUNT_ID:team-fog-alerts
    
    # 메모리 사용률 알람
    aws cloudwatch put-metric-alarm \
        --alarm-name "reservation-service-high-memory" \
        --alarm-description "Reservation Service 메모리 사용률이 높음" \
        --metric-name MemoryUtilization \
        --namespace AWS/ECS \
        --statistic Average \
        --period 60 \
        --threshold 85 \
        --comparison-operator GreaterThanThreshold \
        --evaluation-periods 2 \
        --dimensions Name=ServiceName,Value=$SERVICE_NAME Name=ClusterName,Value=$CLUSTER_NAME \
        --alarm-actions arn:aws:sns:ap-northeast-2:$AWS_ACCOUNT_ID:team-fog-alerts
    
    log_success "CloudWatch 알람 설정 완료"
}

# 3. Redis 캐시 설정
setup_redis_cache() {
    log_info "Redis 캐시 설정 시작..."
    
    # Redis 클러스터 생성
    aws elasticache create-replication-group \
        --replication-group-id team-fog-redis \
        --replication-group-description "Team-FOG Redis Cache for Reservation Service" \
        --node-group-configuration '[
            {
                "PrimaryAvailabilityZone": "ap-northeast-2a",
                "ReplicaAvailabilityZones": ["ap-northeast-2c"],
                "ReplicaCount": 1,
                "Slots": "0-16383"
            }
        ]' \
        --cache-node-type cache.t3.micro \
        --engine redis \
        --engine-version 6.x \
        --port 6379 \
        --subnet-group-name team-fog-redis-subnet-group \
        --security-group-ids sg-redis \
        --cache-parameter-group-family redis6.x \
        --automatic-failover-enabled \
        --multi-az-enabled \
        --num-cache-clusters 2
    
    log_success "Redis 캐시 설정 완료"
}

# 4. 데이터베이스 연결 풀 최적화
setup_database_optimization() {
    log_info "데이터베이스 최적화 설정 시작..."
    
    # RDS 파라미터 그룹 수정
    aws rds modify-db-parameter-group \
        --db-parameter-group-name team-fog-oracle-params \
        --parameters '[
            {
                "ParameterName": "processes",
                "ParameterValue": "300",
                "ApplyMethod": "pending-reboot"
            },
            {
                "ParameterName": "sessions",
                "ParameterValue": "500",
                "ApplyMethod": "pending-reboot"
            },
            {
                "ParameterName": "open_cursors",
                "ParameterValue": "1000",
                "ApplyMethod": "immediate"
            }
        ]'
    
    log_success "데이터베이스 최적화 설정 완료"
}

# 5. 로드 밸런서 설정
setup_load_balancer() {
    log_info "로드 밸런서 설정 시작..."
    
    # 타겟 그룹 생성
    aws elbv2 create-target-group \
        --name team-fog-reservation-tg \
        --protocol HTTP \
        --port 8080 \
        --vpc-id vpc-xxxxx \
        --target-type ip \
        --health-check-path /health \
        --health-check-interval-seconds 30 \
        --health-check-timeout-seconds 5 \
        --healthy-threshold-count 2 \
        --unhealthy-threshold-count 3
    
    # 리스너 규칙 생성
    aws elbv2 create-listener \
        --load-balancer-arn arn:aws:elasticloadbalancing:ap-northeast-2:$AWS_ACCOUNT_ID:loadbalancer/app/team-fog-alb/xxxxx \
        --protocol HTTP \
        --port 80 \
        --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:ap-northeast-2:$AWS_ACCOUNT_ID:targetgroup/team-fog-reservation-tg/xxxxx
    
    log_success "로드 밸런서 설정 완료"
}

# 6. 예약된 스케일링 설정
setup_scheduled_scaling() {
    log_info "예약된 스케일링 설정 시작..."
    
    # 피크 타임 스케일링 (오전 11시)
    aws application-autoscaling put-scheduled-action \
        --service-namespace ecs \
        --scalable-dimension ecs:service:DesiredCount \
        --resource-id service/$CLUSTER_NAME/$SERVICE_NAME \
        --scheduled-action-name peak-time-scaling \
        --schedule "cron(0 11 * * ? *)" \
        --scalable-target-action MinCapacity=5,MaxCapacity=25
    
    # 오프 피크 스케일링 (오후 11시)
    aws application-autoscaling put-scheduled-action \
        --service-namespace ecs \
        --scalable-dimension ecs:service:DesiredCount \
        --resource-id service/$CLUSTER_NAME/$SERVICE_NAME \
        --scheduled-action-name off-peak-scaling \
        --schedule "cron(0 23 * * ? *)" \
        --scalable-target-action MinCapacity=2,MaxCapacity=10
    
    log_success "예약된 스케일링 설정 완료"
}

# 7. 모니터링 대시보드 생성
setup_monitoring_dashboard() {
    log_info "모니터링 대시보드 생성 시작..."
    
    # CloudWatch 대시보드 생성
    aws cloudwatch put-dashboard \
        --dashboard-name "Team-FOG-Reservation-Monitoring" \
        --dashboard-body '{
            "widgets": [
                {
                    "type": "metric",
                    "x": 0,
                    "y": 0,
                    "width": 12,
                    "height": 6,
                    "properties": {
                        "metrics": [
                            ["AWS/ECS", "CPUUtilization", "ServiceName", "'$SERVICE_NAME'", "ClusterName", "'$CLUSTER_NAME'"]
                        ],
                        "view": "timeSeries",
                        "stacked": false,
                        "region": "'$AWS_REGION'",
                        "title": "Reservation Service CPU Utilization"
                    }
                },
                {
                    "type": "metric",
                    "x": 12,
                    "y": 0,
                    "width": 12,
                    "height": 6,
                    "properties": {
                        "metrics": [
                            ["AWS/ECS", "MemoryUtilization", "ServiceName", "'$SERVICE_NAME'", "ClusterName", "'$CLUSTER_NAME'"]
                        ],
                        "view": "timeSeries",
                        "stacked": false,
                        "region": "'$AWS_REGION'",
                        "title": "Reservation Service Memory Utilization"
                    }
                }
            ]
        }'
    
    log_success "모니터링 대시보드 생성 완료"
}

# 8. 백업 및 복구 설정
setup_backup_recovery() {
    log_info "백업 및 복구 설정 시작..."
    
    # RDS 자동 백업 설정
    aws rds modify-db-instance \
        --db-instance-identifier primarydb \
        --backup-retention-period 7 \
        --preferred-backup-window "02:00-03:00" \
        --preferred-maintenance-window "sun:03:00-sun:04:00"
    
    # 스냅샷 정책 설정
    aws rds create-db-snapshot \
        --db-instance-identifier primarydb \
        --db-snapshot-identifier team-fog-backup-$(date +%Y%m%d-%H%M%S)
    
    log_success "백업 및 복구 설정 완료"
}

# 메인 실행 함수
main() {
    log_info "예약 몰림 상황 대비 인프라 설정 시작"
    
    # AWS CLI 확인
    if ! command -v aws &> /dev/null; then
        log_error "AWS CLI가 설치되지 않았습니다."
        exit 1
    fi
    
    # AWS 계정 확인
    if [ -z "$AWS_ACCOUNT_ID" ]; then
        log_error "AWS 계정 ID를 가져올 수 없습니다."
        exit 1
    fi
    
    log_info "AWS 계정 ID: $AWS_ACCOUNT_ID"
    log_info "AWS 리전: $AWS_REGION"
    
    # 각 설정 단계 실행
    setup_auto_scaling
    setup_cloudwatch_alarms
    setup_redis_cache
    setup_database_optimization
    setup_load_balancer
    setup_scheduled_scaling
    setup_monitoring_dashboard
    setup_backup_recovery
    
    log_success "모든 인프라 설정 완료"
    log_info "설정된 리소스:"
    log_info "- 자동 스케일링: 2-20 인스턴스"
    log_info "- CloudWatch 알람: CPU/메모리 모니터링"
    log_info "- Redis 캐시: 성능 향상"
    log_info "- 데이터베이스 최적화: 연결 풀 확장"
    log_info "- 로드 밸런서: 트래픽 분산"
    log_info "- 예약 스케일링: 피크/오프피크 시간대"
    log_info "- 모니터링 대시보드: 실시간 모니터링"
    log_info "- 백업 정책: 7일 보관"
}

# 스크립트 실행
main "$@"
