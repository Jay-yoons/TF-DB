#!/bin/bash

# 🚀 예약 몰림 시나리오 테스트 스크립트
# 한 가게에 동시에 많은 예약이 몰리는 상황을 시뮬레이션

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
BASE_URL="https://reservation-service.team-fog.com"
STORE_ID="store001"
BOOKING_DATE="2024-01-20"
ACCESS_TOKEN="your-access-token-here"

# 테스트 데이터 생성
generate_test_data() {
    log_info "테스트 데이터 생성 중..."
    
    # 테스트용 JSON 파일 생성
    cat > test_booking.json << EOF
{
  "userId": "user\${RANDOM}",
  "storeId": "$STORE_ID",
  "bookingDate": "$BOOKING_DATE",
  "count": \$((RANDOM % 4 + 1))
}
EOF
    
    log_success "테스트 데이터 생성 완료"
}

# 1. 기본 부하 테스트 (100개 요청, 10개 동시)
basic_load_test() {
    log_info "기본 부하 테스트 시작 (100개 요청, 10개 동시)"
    
    ab -n 100 -c 10 -p test_booking.json \
       -T application/json \
       -H "Authorization: Bearer $ACCESS_TOKEN" \
       -H "Content-Type: application/json" \
       "$BASE_URL/api/bookings"
    
    log_success "기본 부하 테스트 완료"
}

# 2. 고부하 테스트 (1000개 요청, 50개 동시)
high_load_test() {
    log_info "고부하 테스트 시작 (1000개 요청, 50개 동시)"
    
    ab -n 1000 -c 50 -p test_booking.json \
       -T application/json \
       -H "Authorization: Bearer $ACCESS_TOKEN" \
       -H "Content-Type: application/json" \
       "$BASE_URL/api/bookings"
    
    log_success "고부하 테스트 완료"
}

# 3. 스트레스 테스트 (5000개 요청, 100개 동시)
stress_test() {
    log_info "스트레스 테스트 시작 (5000개 요청, 100개 동시)"
    
    ab -n 5000 -c 100 -p test_booking.json \
       -T application/json \
       -H "Authorization: Bearer $ACCESS_TOKEN" \
       -H "Content-Type: application/json" \
       "$BASE_URL/api/bookings"
    
    log_success "스트레스 테스트 완료"
}

# 4. 동시성 테스트 (Race Condition 시뮬레이션)
concurrency_test() {
    log_info "동시성 테스트 시작 (Race Condition 시뮬레이션)"
    
    # 동일한 가게, 동일한 날짜, 동일한 시간에 100개 요청
    for i in {1..100}; do
        cat > "test_booking_$i.json" << EOF
{
  "userId": "user$i",
  "storeId": "$STORE_ID",
  "bookingDate": "$BOOKING_DATE",
  "count": 2
}
EOF
    done
    
    # 모든 요청을 동시에 실행
    for i in {1..100}; do
        ab -n 1 -c 1 -p "test_booking_$i.json" \
           -T application/json \
           -H "Authorization: Bearer $ACCESS_TOKEN" \
           -H "Content-Type: application/json" \
           "$BASE_URL/api/bookings" &
    done
    
    # 모든 백그라운드 프로세스 완료 대기
    wait
    
    log_success "동시성 테스트 완료"
}

# 5. 용량 한계 테스트
capacity_limit_test() {
    log_info "용량 한계 테스트 시작"
    
    # 가게의 총 좌석 수를 초과하는 예약 요청
    cat > capacity_test.json << EOF
{
  "userId": "capacity_test_user",
  "storeId": "$STORE_ID",
  "bookingDate": "$BOOKING_DATE",
  "count": 1000
}
EOF
    
    ab -n 10 -c 5 -p capacity_test.json \
       -T application/json \
       -H "Authorization: Bearer $ACCESS_TOKEN" \
       -H "Content-Type: application/json" \
       "$BASE_URL/api/bookings"
    
    log_success "용량 한계 테스트 완료"
}

# 6. 응답 시간 모니터링
response_time_monitoring() {
    log_info "응답 시간 모니터링 시작"
    
    # 1분간 지속적인 요청으로 응답 시간 변화 관찰
    ab -n 600 -c 10 -p test_booking.json \
       -T application/json \
       -H "Authorization: Bearer $ACCESS_TOKEN" \
       -H "Content-Type: application/json" \
       -g response_time_results.tsv \
       "$BASE_URL/api/bookings"
    
    log_success "응답 시간 모니터링 완료"
    log_info "결과 파일: response_time_results.tsv"
}

# 7. 에러율 테스트
error_rate_test() {
    log_info "에러율 테스트 시작"
    
    # 잘못된 데이터로 에러율 측정
    cat > error_test.json << EOF
{
  "userId": "",
  "storeId": "invalid_store",
  "bookingDate": "invalid_date",
  "count": -1
}
EOF
    
    ab -n 100 -c 10 -p error_test.json \
       -T application/json \
       -H "Authorization: Bearer $ACCESS_TOKEN" \
       -H "Content-Type: application/json" \
       "$BASE_URL/api/bookings"
    
    log_success "에러율 테스트 완료"
}

# 메인 실행 함수
main() {
    log_info "예약 몰림 시나리오 테스트 시작"
    
    # Apache Bench 설치 확인
    if ! command -v ab &> /dev/null; then
        log_error "Apache Bench가 설치되지 않았습니다."
        log_info "Ubuntu/Debian: sudo apt-get install apache2-utils"
        log_info "CentOS/RHEL: sudo yum install httpd-tools"
        exit 1
    fi
    
    # 테스트 데이터 생성
    generate_test_data
    
    # 테스트 실행
    case "${1:-all}" in
        "basic")
            basic_load_test
            ;;
        "high")
            high_load_test
            ;;
        "stress")
            stress_test
            ;;
        "concurrency")
            concurrency_test
            ;;
        "capacity")
            capacity_limit_test
            ;;
        "monitoring")
            response_time_monitoring
            ;;
        "error")
            error_rate_test
            ;;
        "all")
            log_info "모든 테스트 실행"
            basic_load_test
            high_load_test
            stress_test
            concurrency_test
            capacity_limit_test
            response_time_monitoring
            error_rate_test
            ;;
        *)
            log_error "알 수 없는 테스트 타입: $1"
            log_info "사용법: $0 [basic|high|stress|concurrency|capacity|monitoring|error|all]"
            exit 1
            ;;
    esac
    
    # 정리
    rm -f test_booking*.json error_test.json capacity_test.json
    
    log_success "모든 테스트 완료"
}

# 스크립트 실행
main "$@"
