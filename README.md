# team7 OOTD (Outfit of The Day) 🌟

> 날씨 기반 패션 추천 및 소셜 플랫폼

## 👥 팀원

## 📖 프로젝트 소개

OOTD는 실시간 날씨 정보를 기반으로 개인화된 패션 추천을 제공하고, 사용자들이 자신의 옷차림을 공유할 수 있는 소셜 플랫폼입니다. 날씨에 맞는 최적의 패션 코디네이션을 추천하고, 다른 사용자들과 패션 영감을 공유할 수 있습니다.

## ✨ 주요 기능

### 🌤️ 날씨 기반 패션 추천
- 실시간 날씨 데이터 수집 및 분석
- 기온, 습도, 강수량 등을 고려한 개인화 추천
- 지역별 맞춤 패션 코디네이션 제안

### 👔 의류 관리
- 개인 의류 아이템 등록 및 카테고리 관리
- 착용 기록 및 선호도 분석
- 시즌별 의류 분류 및 추천

### 📱 소셜 피드
- 오늘의 옷차림(OOTD) 사진 공유
- 팔로우/팔로워 시스템
- 좋아요 및 댓글 기능

### 🔔 스마트 알림
- 날씨 변화 알림
- 옷차림 추천 푸시 알림
- 실시간 웹소켓 기반 알림 시스템

### 🎯 개인화 추천
- 사용자 선호도 학습
- 과거 착용 기록 기반 추천
- 스타일 분석 및 트렌드 제안

## 🛠️ 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Database**: PostgreSQL, MongoDB, Redis
- **Security**: Spring Security, JWT, OAuth2
- **Messaging**: Apache Kafka
- **Batch**: Spring Batch
- **Cache**: Caffeine Cache
- **API Documentation**: Swagger (OpenAPI 3.0)

### Infrastructure
- **Cloud**: AWS (ECS, S3, CloudWatch)
- **Containerization**: Docker, Docker Compose
- **CI/CD**: GitHub Actions
- **Monitoring**: Prometheus, Grafana
- **Database**: AWS RDS (PostgreSQL)

### Development Tools
- **Build Tool**: Gradle
- **Code Quality**: JaCoCo (Test Coverage)
- **ORM**: JPA, QueryDSL
- **Mapping**: MapStruct
- **Validation**: Bean Validation

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │    │   Web Client    │    │  Admin Panel    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────┐
                    │   Load Balancer │
                    └─────────────────┘
                                 │
                    ┌─────────────────┐
                    │  Spring Boot    │
                    │   Application   │
                    └─────────────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              │                  │                  │
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │   PostgreSQL    │ │    MongoDB      │ │     Redis       │
    │   (Main DB)     │ │  (Document)     │ │    (Cache)      │
    └─────────────────┘ └─────────────────┘ └─────────────────┘
```

## 📋 API 엔드포인트

### 인증 & 사용자
- `POST /api/auth/login` - 로그인
- `POST /api/auth/register` - 회원가입
- `POST /api/auth/refresh` - 토큰 갱신
- `GET /api/users/profile` - 프로필 조회
- `PUT /api/users/profile` - 프로필 수정

### 날씨 & 추천
- `GET /api/weather/current` - 현재 날씨 조회
- `GET /api/weather/forecast` - 날씨 예보
- `GET /api/recommend/outfit` - 의상 추천
- `GET /api/recommend/clothes` - 의류 추천

### 피드 & 소셜
- `GET /api/feeds` - 피드 목록
- `POST /api/feeds` - 피드 작성
- `POST /api/feeds/{id}/like` - 좋아요
- `GET /api/follows` - 팔로우 목록
- `POST /api/follows/{userId}` - 팔로우

### 의류 관리
- `GET /api/clothes` - 의류 목록
- `POST /api/clothes` - 의류 등록
- `PUT /api/clothes/{id}` - 의류 수정
- `DELETE /api/clothes/{id}` - 의류 삭제

## 🚀 시작하기

### 필요 사항
- Java 17 이상
- Docker & Docker Compose
- PostgreSQL 13+
- Redis 6+

### 로컬 개발 환경 설정

1. **레포지토리 클론**
   ```bash
   git clone https://github.com/your-org/ootd.git
   cd ootd
   ```

2. **환경 변수 설정**
   ```bash
   cp .env.example .env
   # .env 파일에서 필요한 환경 변수 설정
   ```

3. **의존성 설치 및 빌드**
   ```bash
   ./gradlew clean build
   ```

4. **데이터베이스 실행 (Docker Compose)**
   ```bash
   docker-compose up -d
   ```

5. **애플리케이션 실행**
   ```bash
   ./gradlew bootRun
   ```

6. **API 문서 확인**
   ```
   http://localhost:8080/swagger-ui.html
   ```

### Docker로 실행

```bash
# 이미지 빌드
docker build -t ootd .

# 컨테이너 실행
docker run -p 8080:8080 ootd
```

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport

# 커버리지 확인
open build/reports/jacoco/test/html/index.html
```

## 📊 모니터링

### Prometheus & Grafana 설정

```bash
# 모니터링 스택 실행
docker-compose up prometheus grafana

# Grafana 접속
# URL: http://localhost:3000
# ID/PW: admin/admin
```

### 주요 메트릭
- Application Health: `/actuator/health`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

## 🔧 환경 변수

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 실행 프로필 | `dev` |
| `DATASOURCE_URL` | 데이터베이스 URL | `jdbc:postgresql://localhost:5432/ootd` |
| `REDIS_HOST` | Redis 호스트 | `localhost` |
| `AWS_S3_BUCKET` | S3 버킷명 | - |
| `JWT_SECRET` | JWT 시크릿 키 | - |

## 📝 개발 가이드

### 코드 스타일
- Google Java Style Guide 적용
- IntelliJ Code Style: `intellij-java-google-style.xml`

### 브랜치 전략
- `main`: 프로덕션 브랜치
- `develop`: 개발 브랜치
- `feature/*`: 기능 개발 브랜치
- `hotfix/*`: 핫픽스 브랜치

### 커밋 메시지
```
feat: 새로운 기능 추가
fix: 버그 수정
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 코드
chore: 빌드 설정 등
```

## 🚀 배포

### CI/CD 파이프라인
- **테스트**: Pull Request 시 자동 테스트 실행
- **빌드**: main 브랜치 푸시 시 Docker 이미지 빌드
- **배포**: AWS ECS로 자동 배포

### 배포 환경
- **개발**: AWS ECS (develop 브랜치)
- **프로덕션**: AWS ECS (main 브랜치)


**Made with ❤️ by OOTD Team**
