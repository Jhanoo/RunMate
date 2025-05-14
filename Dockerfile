# 1) Build stage: Gradle 8.10 + JDK17
FROM gradle:8.10-jdk17 AS builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . .
RUN gradle clean bootJar -x test

# 2) Runtime stage: JRE only
FROM openjdk:17-jdk-slim
WORKDIR /app

# Timezone 환경변수 설정 및 tzdata 설치
ENV TZ=Asia/Seoul
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul"

# tzdata 설치 및 /etc/localtime 링크
RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y tzdata \
    && ln -fs /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
    && echo "Asia/Seoul" > /etc/timezone \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
    
COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

# 로그 디렉토리 생성
RUN mkdir -p /app/logs && chmod 777 /app/logs

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
