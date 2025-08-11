# 하이브리드 클라우드 기반 MSA 인프라 구축 프로젝트

## 프로젝트 개요
이 프로젝트는 여러 팀 프로젝트에서 공통으로 사용되는 하이브리드 클라우드 기반 MSA(Microservice Architecture) 인프라 구축 예시입니다.

## 주요 기술 스택

### 클라우드 플랫폼
- **AWS**: 주요 클라우드 플랫폼
- **Azure**: 보조 클라우드 플랫폼 (하이브리드 구성)
- **온프레미스**: 기존 인프라와의 연동

### MSA 아키텍처
- **Spring Cloud**: 마이크로서비스 프레임워크
- **Docker**: 컨테이너화
- **Kubernetes**: 오케스트레이션
- **Istio**: 서비스 메시

### 멀티리전 구성
- **AWS Region**: us-east-1, us-west-2, ap-northeast-2
- **Azure Region**: East US, West US 2, Korea Central
- **재해대비**: Active-Active 구성

### 모니터링 및 로깅
- **Prometheus**: 메트릭 수집
- **Grafana**: 대시보드
- **ELK Stack**: 로그 분석
- **Jaeger**: 분산 추적

## 프로젝트 구조
```
hybrid-cloud-msa/
├── infrastructure/
│   ├── terraform/
│   ├── kubernetes/
│   └── docker/
├── services/
│   ├── user-service/
│   ├── order-service/
│   ├── payment-service/
│   └── notification-service/
├── api-gateway/
├── monitoring/
└── docs/
```

## 빠른 시작
1. `infrastructure/terraform/` 디렉토리에서 인프라 프로비저닝
2. `services/` 디렉토리에서 마이크로서비스 배포
3. `monitoring/` 디렉토리에서 모니터링 설정

## 팀별 활용 방안
- **Voyagers 팀**: 대규모 사용자 기반 서비스
- **BeeMSA 팀**: MSA 전용 아키텍처
- **Aurora 팀**: 고성능 데이터 처리
- **Sprout 팀**: 실시간 데이터 스트리밍
- **EYELESS 팀**: AI/ML 서비스 통합
- **클딱 팀**: 게임 서버 아키텍처 