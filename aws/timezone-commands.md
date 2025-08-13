# 🕐 AWS ECS 한국시간대 직접 설정 명령어

## 1. **환경변수 직접 설정**

### User Service 시간대 설정
```bash
# 현재 태스크 정의 가져오기
aws ecs describe-task-definition \
  --task-definition user-service \
  --region ap-northeast-2 \
  --query 'taskDefinition' \
  --output json > current-task-def.json

# 환경변수 추가 (jq 사용)
jq '.containerDefinitions[0].environment += [
  {"name": "TZ", "value": "Asia/Seoul"},
  {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"}
]' current-task-def.json > updated-task-def.json

# 새 태스크 정의 등록
aws ecs register-task-definition \
  --cli-input-json file://updated-task-def.json \
  --region ap-northeast-2

# 서비스 업데이트
aws ecs update-service \
  --cluster team-fog-cluster \
  --service team-fog-user-service \
  --task-definition user-service:$(aws ecs describe-task-definitions \
    --task-definitions user-service \
    --region ap-northeast-2 \
    --query 'taskDefinitions[0].revision' \
    --output text) \
  --region ap-northeast-2
```

### Store Service 시간대 설정
```bash
# Store Service용 동일한 과정
aws ecs describe-task-definition \
  --task-definition store-service-task \
  --region ap-northeast-2 \
  --query 'taskDefinition' \
  --output json > store-task-def.json

jq '.containerDefinitions[0].environment += [
  {"name": "TZ", "value": "Asia/Seoul"},
  {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"}
]' store-task-def.json > store-updated-task-def.json

aws ecs register-task-definition \
  --cli-input-json file://store-updated-task-def.json \
  --region ap-northeast-2

aws ecs update-service \
  --cluster team-fog-cluster \
  --service team-fog-store-service \
  --task-definition store-service-task:$(aws ecs describe-task-definitions \
    --task-definitions store-service-task \
    --region ap-northeast-2 \
    --query 'taskDefinitions[0].revision' \
    --output text) \
  --region ap-northeast-2
```

### Reservation Service 시간대 설정
```bash
# Reservation Service용 동일한 과정
aws ecs describe-task-definition \
  --task-definition reservation-service-task \
  --region ap-northeast-2 \
  --query 'taskDefinition' \
  --output json > reservation-task-def.json

jq '.containerDefinitions[0].environment += [
  {"name": "TZ", "value": "Asia/Seoul"},
  {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"}
]' reservation-task-def.json > reservation-updated-task-def.json

aws ecs register-task-definition \
  --cli-input-json file://reservation-updated-task-def.json \
  --region ap-northeast-2

aws ecs update-service \
  --cluster team-fog-cluster \
  --service team-fog-reservation-service \
  --task-definition reservation-service-task:$(aws ecs describe-task-definitions \
    --task-definitions reservation-service-task \
    --region ap-northeast-2 \
    --query 'taskDefinitions[0].revision' \
    --output text) \
  --region ap-northeast-2
```

## 2. **설정 확인 명령어**

### 현재 환경변수 확인
```bash
# User Service 환경변수 확인
aws ecs describe-tasks \
  --cluster team-fog-cluster \
  --tasks $(aws ecs list-tasks \
    --cluster team-fog-cluster \
    --service-name team-fog-user-service \
    --region ap-northeast-2 \
    --query 'taskArns[0]' \
    --output text) \
  --region ap-northeast-2 \
  --query 'tasks[0].overrides.containerOverrides[0].environment[?name==`TZ` || name==`JAVA_OPTS`]' \
  --output table
```

### 로그에서 시간대 확인
```bash
# User Service 로그에서 시간대 확인
aws logs filter-log-events \
  --log-group-name "/ecs/team-fog-user-service" \
  --region ap-northeast-2 \
  --start-time $(date -d '1 hour ago' +%s)000 \
  --filter-pattern "timezone OR time OR Asia/Seoul" \
  --query 'events[*].message' \
  --output text
```

## 3. **한 번에 모든 서비스 업데이트**

```bash
#!/bin/bash
# 모든 서비스 시간대 설정 스크립트

SERVICES=("user-service" "store-service-task" "reservation-service-task")
SERVICE_NAMES=("team-fog-user-service" "team-fog-store-service" "team-fog-reservation-service")

for i in "${!SERVICES[@]}"; do
    TASK_DEF="${SERVICES[$i]}"
    SERVICE_NAME="${SERVICE_NAMES[$i]}"
    
    echo "업데이트 중: $SERVICE_NAME"
    
    # 현재 태스크 정의 가져오기
    aws ecs describe-task-definition \
      --task-definition $TASK_DEF \
      --region ap-northeast-2 \
      --query 'taskDefinition' \
      --output json > temp-task-def.json
    
    # 환경변수 추가
    jq '.containerDefinitions[0].environment += [
      {"name": "TZ", "value": "Asia/Seoul"},
      {"name": "JAVA_OPTS", "value": "-Duser.timezone=Asia/Seoul -Dfile.encoding=UTF-8"}
    ]' temp-task-def.json > temp-updated-task-def.json
    
    # 새 태스크 정의 등록
    aws ecs register-task-definition \
      --cli-input-json file://temp-updated-task-def.json \
      --region ap-northeast-2
    
    # 서비스 업데이트
    aws ecs update-service \
      --cluster team-fog-cluster \
      --service $SERVICE_NAME \
      --task-definition $TASK_DEF:$(aws ecs describe-task-definitions \
        --task-definitions $TASK_DEF \
        --region ap-northeast-2 \
        --query 'taskDefinitions[0].revision' \
        --output text) \
      --region ap-northeast-2
    
    echo "완료: $SERVICE_NAME"
done

# 임시 파일 정리
rm -f temp-task-def.json temp-updated-task-def.json
```

## 4. **시간대 설정 검증**

```bash
# 모든 서비스의 시간대 설정 확인
for SERVICE in "team-fog-user-service" "team-fog-store-service" "team-fog-reservation-service"; do
    echo "=== $SERVICE ==="
    aws ecs describe-tasks \
      --cluster team-fog-cluster \
      --tasks $(aws ecs list-tasks \
        --cluster team-fog-cluster \
        --service-name $SERVICE \
        --region ap-northeast-2 \
        --query 'taskArns[0]' \
        --output text) \
      --region ap-northeast-2 \
      --query 'tasks[0].overrides.containerOverrides[0].environment[?name==`TZ` || name==`JAVA_OPTS`]' \
      --output table
done
```

## 5. **주의사항**

- **jq 설치 필요**: `sudo apt-get install jq` (Ubuntu/Debian) 또는 `sudo yum install jq` (CentOS/RHEL)
- **권한 확인**: ECS 서비스 업데이트 권한이 필요합니다
- **무중단 배포**: 새 태스크가 정상적으로 시작된 후 기존 태스크가 종료됩니다
- **로그 확인**: 설정 후 CloudWatch 로그에서 시간대 관련 메시지를 확인하세요
