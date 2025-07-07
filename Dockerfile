# 멀티 스테이지 빌드를 사용하여 빌드와 런타임 분리
FROM gradle:8.5-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 설정 파일들 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 효율성을 위해)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드
RUN gradle bootJar --no-daemon

# 실행 이미지
FROM eclipse-temurin:17-jre

# curl 설치 (헬스체크용)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 운영 환경 설정
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC"

# 애플리케이션을 실행할 사용자 생성 (보안상 root 사용 지양)
RUN groupadd -r ootd && useradd -r -g ootd ootd

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/ootd-0.0.1-SNAPSHOT.jar app.jar

# 파일 소유권 변경
RUN chown ootd:ootd app.jar

# 애플리케이션 사용자로 변경
USER ootd

# 포트 노출
EXPOSE 8080

# 헬스체크 추가 - 단순히 포트 연결 확인
HEALTHCHECK --interval=30s --timeout=10s --start-period=180s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]