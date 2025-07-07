# ECS 배포 트러블슈팅 가이드

## 개요
OOTD 애플리케이션의 AWS ECS Fargate 환경 배포 중 발생한 문제들과 해결 과정을 정리한 문서입니다.

## 초기 상황
- **브랜치**: `feature/infra`
- **배포 방식**: 수동 ECR 푸시 및 ECS 배포
- **인프라**: ECS Fargate + Application Load Balancer
- **애플리케이션**: Spring Boot 3.5.3 + Java 17

## 발생한 문제들과 해결 과정

### GitHub Actions 배포 제한 문제
**문제**: 워크플로우가 `main` 브랜치에서만 배포하도록 설정됨

**해결**: 수동 ECR 푸시로 우회
```bash
# Docker 빌드 및 ECR 푸시 -> AMD64 배포
docker build --platform linux/amd64 -t ootd .
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin 826421662185.dkr.ecr.ap-northeast-2.amazonaws.com
docker tag ootd:latest 826421662185.dkr.ecr.ap-northeast-2.amazonaws.com/ootd:latest
docker push 826421662185.dkr.ecr.ap-northeast-2.amazonaws.com/ootd:latest
```


### AWS S3 인증 키 누락 문제
**문제**: 애플리케이션 시작 실패
```
Access key ID cannot be blank
```

**원인**: S3 접근을 위한 AWS 키가 환경변수에 빈 값으로 설정됨

**해결**: AWS Systems Manager Parameter Store 활용
```bash
# Parameter Store에 키 저장
aws ssm put-parameter --name "/ootd/s3/access-key" --value "." --type "SecureString"
aws ssm put-parameter --name "/ootd/s3/secret-key" --value "." --type "SecureString"
```

**태스크 정의 수정**: 환경변수에서 secrets로 이동
```json
"secrets": [
  {
    "name": "AWS_S3_ACCESS_KEY",
    "valueFrom": "arn:aws:ssm:ap-northeast-2:826421662185:parameter/ootd/s3/access-key"
  },
  {
    "name": "AWS_S3_SECRET_KEY",
    "valueFrom": "arn:aws:ssm:ap-northeast-2:826421662185:parameter/ootd/s3/secret-key"
  }
]
```

### 헬스체크 실패 문제
**문제**: 로드밸런서에서 지속적인 헬스체크 실패

#### 애플리케이션 시작 시간 문제
**문제**: Spring Boot 시작 시간(98초)이 헬스체크 대기 시간보다 김

**해결**: 헬스체크 설정 조정
```json
"healthCheck": {
  "startPeriod": 180,  // 60 → 180초로 증가
  "timeout": 10,       // 5 → 10초로 증가
  "retries": 5         // 3 → 5회로 증가
}
```

#### Actuator 엔드포인트 문제
**문제**: `/actuator/health` 엔드포인트 응답 없음

**해결**: 헬스체크 경로를 루트(`/`)로 변경
```json
"healthCheck": {
  "command": ["CMD-SHELL", "curl -f http://localhost:8080/ || exit 1"]
}
```

#### 로드밸런서 설정 불일치
**문제**: ECS와 로드밸런서가 다른 헬스체크 경로 사용
- ECS: `/` (수정됨)
- 로드밸런서: `/actuator/health` (기존)

**해결**: 로드밸런서 타겟 그룹 설정 동기화
```bash
aws elbv2 modify-target-group \
  --target-group-arn arn:aws:elasticloadbalancing:ap-northeast-2:826421662185:targetgroup/ootd-group/533170ff932a4cd7 \
  --health-check-path "/"
```

#### Request Timeout 문제
**문제**: 루트 경로(`/`) 요청 시 타임아웃 발생

**해결**: 더 단순한 헬스체크로 변경
- **ECS**: TCP 포트 연결 확인 `nc -z localhost 8080`
- **로드밸런서**: 더 관대한 설정 (타임아웃 10초, 재시도 5회)

#### Spring Boot 시작 시간 문제
**문제**: Spring Boot 애플리케이션이 완전히 시작되는 데 2-3분이 걸림
- MongoDB 연결 설정 중 30초 대기
- 여러 Spring Data 모듈 스캔 및 초기화 시간 소요

**해결**: 헬스체크 시간을 애플리케이션 시작 시간에 맞춰 대폭 증가
- **ECS 태스크 정의**: startPeriod를 300초(5분)로 증가
- **로드밸런서**: 인터벌 60초, 타임아웃 30초, 실패 임계값 10회로 관대하게 설정

### `spring.batch.jdbc.initialize-schema` 설정 문제
**문제**: 애플리케이션 시작 시 `spring.batch.jdbc.initialize-schema: always` 설정으로 인해 배치 테이블 초기화 시도 및 시작 지연/실패.
**원인**: `application.yml`의 기본 설정이 `prod` 환경에도 적용되어 발생.
**해결**: `src/main/resources/application-prod.yml` 파일에 `spring.batch.jdbc.initialize-schema: never` 추가.

### 태스크 정의 등록 시 이미지 경로 변수 문제
**문제**: `aws ecs register-task-definition` 사용 시 `Container.image`에 변수(`$`)가 포함되어 오류 발생.
**원인**: `cli-input-json`은 JSON 파일 내 변수를 해석하지 못함.
**해결**: `ecs-task-definition.json`을 임시 파일로 복사 후, 이미지 경로 변수를 실제 ECR 이미지 주소로 치환하여 등록.

### 헬스체크 경로 불일치 및 지속적인 실패
**문제**: 로드밸런서와 ECS 태스크 정의 간 헬스체크 경로 불일치로 인한 헬스체크 실패.
**원인**: 로드밸런서 헬스체크 경로를 `/health-check`로 변경했으나, ECS 태스크 정의에는 반영되지 않음.
**해결**:
- `/health-check` 엔드포인트 추가 (HealthCheckController.java).
- `SecurityConfig.java`에서 `/health-check` 경로 인증 제외.
- 로드밸런서 대상 그룹 헬스체크 경로를 `/health-check`로 변경.
- ECS 태스크 정의 `healthCheck` command를 `curl -f http://localhost:8080/health-check || exit 1`로 변경.
- 헬스체크 경로를 `/actuator/health`로 되돌림.
### 현재 헬스 체크 설정:
```json
            "healthCheck": {
                "command": [
                    "CMD-SHELL",
                    "curl -f http://localhost:8080/health/check || exit 1"
                ],
                "interval": 60, // 두 번의 헬스체크 호출 사이 간격
                "timeout": 30, // 30초 안에 결과를 못 받으면 실패
                "retries": 5, // 5번 연속 실패시 헬스체크 실패로 간주
                "startPeriod": 300 // 5분 까지는 실패 무시
            }
```

### 지속적인 헬스체크 실패 (하지 말 것)
**문제**: 헬스체크 경로를 일치시키고 애플리케이션이 시작 로그를 남김에도 불구하고 헬스체크 지속 실패.
**원인**: Spring Boot 애플리케이션의 높은 메모리 사용량으로 인한 메모리 부족 가능성.
**해결 방안**: ECS 태스크 정의의 메모리 할당량을 1GB에서 2GB로 증량?

### ECS 상태 검사 유예기간 설정
**문제**: 애플리케이션이 정상적으로 실행 상태이지만 헬스체크 실패.
**원인**: ECS 상태 검사 유예기간 설정이 안되어 있음.
**해결 방안**: 가용영역 리밸런싱 체크, 상태 검사 유예기간 300초 설정.

### 현재 상황 분석 (2025-07-07 09:43)
**문제**: 
- ECS 서비스가 지속적으로 헬스체크 실패로 태스크 재시작
- 로드밸런서 타겟이 `initial`/`draining` 상태
- Spring Boot 애플리케이션은 시작되지만 완전 초기화 전에 종료됨

**원인 분석**:
1. **메모리 부족**: 현재 1GB 할당, Spring Boot + JPA + MongoDB 연결에는 부족
2. **헬스체크 타이밍**: 애플리케이션 완전 초기화 전에 헬스체크 시도
3. **로드밸런서 헬스체크**: 더 관대한 설정 필요

**해결 방안**:
1. ~~**메모리 증량**: 1GB → 2GB로 증량~~ (메모리 문제 아님)
2. ✅ **보안 그룹 설정**: 8080 포트 인바운드 규칙 추가
3. ✅ **헬스체크 경로 수정**: `/health-check` → `/actuator/health`

**실제 원인 및 해결**:
- ❌ 메모리 부족이 아님 (애플리케이션 정상 시작됨)
- ✅ 보안 그룹에서 8080 포트가 차단되어 있었음 → 인바운드 규칙 추가
- ✅ 존재하지 않는 `/health-check` 엔드포인트로 헬스체크 시도 → `/actuator/health`로 변경
- ✅ **핵심 문제**: Spring Security에서 `/actuator/health` 경로가 인증 필요 → `permitAll()` 추가

**최종 해결책 (2025-07-07 10:20)**:
1. SecurityConfig.java에 `/actuator/health` 경로를 `permitAll()` 추가
2. 새 Docker 이미지 빌드 및 ECR 푸시
3. ECS 서비스 강제 재배포로 HTTP 401 오류 해결

### HTTP 503 Service Unavailable 오류 (2025-07-07 10:30)
**문제**: HTTP 401 오류 해결 후 503 Service Unavailable 오류 발생
**원인**: Spring Boot Actuator 헬스체크에서 Redis와 Mail 컴포넌트 실패
```
2025-07-07T01:36:41.639Z WARN --- [boundedElastic-2] o.s.b.a.d.r.RedisReactiveHealthIndicator : Redis health check failed
2025-07-07T01:36:41.638Z WARN --- [http-nio-8080-exec-5] o.s.b.actuate.mail.MailHealthIndicator : Mail health check failed
```

**해결**: application-prod.yml에 Redis와 Mail 헬스체크 비활성화
```yaml
management:
  health:
    redis:
      enabled: false
    mail:
      enabled: false
```

### 배포에 성공했지만, 10분마다 헬스체크 오류나서 재배포되는 상황
**문제**: 배포에는 성공했지만 헬스체크 오류가 나서 10분마다 서버가 재배포.
**원인**: Dockerfile에 헬스체크 경로를 /actuator/health로 변경.


**교훈**: 
- Spring Boot Actuator는 모든 컴포넌트 헬스체크가 성공해야 전체 health 엔드포인트가 성공
- 프로덕션 환경에서 사용하지 않는 컴포넌트의 헬스체크는 비활성화 필요

## 기록

### 1. 보안 모범 사례
- 민감한 정보는 환경변수 대신 Parameter Store 사용
- SecureString 타입으로 암호화 저장

### 2. 헬스체크 설계
- 애플리케이션 특성에 맞는 헬스체크 경로 선택
- ECS와 로드밸런서 설정 일치 확인
- Spring Boot 시작 시간을 고려한 대기 시간 설정

### 3. 배포 전략
- 태스크 정의 버전 관리의 중요성
- ECR 이미지 태그 일관성 유지
- 로그 모니터링을 통한 빠른 문제 진단

### 4. 트러블슈팅 접근법
- 계층별 문제 분석: ECR → ECS → 로드밸런서 → 애플리케이션
- 로그 우선 확인: CloudWatch Logs를 통한 실시간 진단
- 점진적 완화: 복잡한 설정에서 단순한 설정으로

## 모니터링 명령어

### 배포 상태 확인
```bash
# ECS 서비스 상태
aws ecs describe-services --cluster ootd-cluster --services ootd-service --region ap-northeast-2

# 태스크 상태
aws ecs list-tasks --cluster ootd-cluster --service-name ootd-service --region ap-northeast-2

# 로드밸런서 타겟 헬스
aws elbv2 describe-target-health --target-group-arn arn:aws:elasticloadbalancing:ap-northeast-2:826421662185:targetgroup/ootd-group/533170ff932a4cd7 --region ap-northeast-2
```

### 로그 확인
```bash
# 최신 로그 스트림
aws logs describe-log-streams --log-group-name "/ecs/ootd-task" --order-by LastEventTime --descending --max-items 1 --region ap-northeast-2

# 특정 로그 스트림 내용
aws logs get-log-events --log-group-name "/ecs/ootd-task" --log-stream-name "ecs/ootd/[TASK-ID]" --region ap-northeast-2
```
