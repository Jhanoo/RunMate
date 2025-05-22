# 🛠️ 포팅 메뉴얼 (Porting Manual)

---

```
📁 포팅 매뉴얼
├── 📄 01. 개발 도구
├── 📄 02. 개발 환경
├── 📄 03. 환경 변수
├── 📄 04. CI/CD 구축
└── 📄 05. 외부 서비스 사용

```

---

## 📄 01. 개발 도구

| 항목 | 내용 |
| --- | --- |
| **IDE** | IntelliJ 2023.3.8, Visual Studio Code 1.97.2 |
| **API 테스트** | Swagger |
| **형상관리** | Git + GitLab |
| **이슈관리** | Jira, Mettamost |
| **문서/디자인 툴** | Notion, Figma, Figjam |

---

## 📄 02. 개발 환경

### (backend)

| 항목 | 버전 / 설명 |
| --- | --- |
| **Language** | Java 17, Node.js v22.13.0, Python 3.9.22 |
| **Framework** | Spring Boot 3.4.5 |
| **npm** | v11.2.0 |
| **Build Tool** | Gradle 8.10 |
| **ORM** | MyBatis |
| **Database** | PostgreSQL 5.12 |
| **API 문서화** | Springdoc OpenAPI (Swagger) 3.1 |
| **API 테스트 도구** | Swagger UI |
| **CI/CD** | Docker 26.1.3, Docker-compose 1.29.2, jenkins 2.492.3 |
| **형상관리** | Git + GitLab |

### (Android)

| 항목 | 버전 / 설명 |
| --- | --- |
| **Android Gradle Plugin (AGP)** | 8.5.0 |
| **Android SDK** | 2.12.8 (타겟/컴파일 기준으로 추정) |
| **Build System** | Gradle |
| **Dependency Injection** | Hilt 2.51.1 |
| **Serialization** | Moshi 1.13.0 |
| **Jetpack Compose** | Compose BOM 2023.08.00 |
| **Compose Material** | 1.3.0 |
| **Navigation** | Navigation Component 2.8.9 |
| **Location** | play-services-location 21.3.0 |
| **Google Mobile Services (GMS)** | gms 19.0.0 |
| **Image Loading** | Glide 4.16.0 |
| **Chart Library** | MPAndroidChart v3.1.0 |

---

### 📁 루트 디렉토리 구조

```
📁 runmate-backend
├── 📁 nginx
├── 📁 postgres-init
├── ⚙️  .env
├── 🐳 docker-compose.yml
├── 🐋 Dockerfile
├── 🛠️  Jenkinsfile

```

### 📌 설명

| 항목 | 설명 |
| --- | --- |
| `nginx/` | Nginx 리버스 프록시 및 SSL 설정 디렉토리 |
| `postgres-init/` | PostgreSQL 초기화 스크립트 디렉토리 |
| `.env` | 환경 변수 파일 (.gitignore 처리 권장) |
| `Dockerfile` | 백엔드 서비스용 Docker 이미지 빌드 파일 |
| `docker-compose.yml` | DB, Redis, 백엔드 등을 함께 실행하기 위한 Docker Compose 설정 |
| `Jenkinsfile` | Jenkins CI/CD 파이프라인 정의 파일 |

### 📁 Node.js 프로젝트 구조

```
nodejs/
├── .env                    # 환경변수 설정
├── package.json            # 의존성 및 실행 스크립트
├── tsconfig.json           # TypeScript 컴파일 설정
└── src/
    ├── index.ts            # 서버 진입점 (HTTP 서버 + Socket.IO 실행)
    ├── app.ts              # Express 설정 (CORS, JSON 파싱, 에러 핸들러 등)
    ├── config/
    │   └── index.ts        # dotenv를 통한 환경 변수 로드
    └── socket/
        ├── index.ts        # Socket.IO 초기화 및 인증 미들웨어 설정
        └── group.events.ts # 그룹 러닝 관련 Socket 이벤트 핸들러

```

---

### 💡 구성 요약

| 디렉토리 / 파일 | 설명 |
| --- | --- |
| `package.json` | 의존성 모듈 및 실행 스크립트 정의 |
| `tsconfig.json` | TypeScript 컴파일 규칙 설정 |
| `src/index.ts` | 애플리케이션의 메인 엔트리 포인트 |
| `src/app.ts` | Express 설정 파일 |
| `src/config/index.ts` | 환경 변수 로드 및 설정 초기화 |
| `src/socket/` | 실시간 통신 로직 (Socket.IO) 모듈 구성 |

## 📄 프로젝트 구조 - `marathon-crawler`

```
marathon-crawler/
│
├── src/                          # 소스 코드 디렉토리
│   ├── crawler/                  # 크롤링 관련 로직
│   │   ├── __init__.py
│   │   ├── marathon_crawler.py   # 메인 크롤링 실행 로직
│   │   └── parser.py             # HTML 파싱 함수 정의
│   │
│   ├── database/                 # DB 연동 모듈
│   │   ├── __init__.py
│   │   ├── models.py             # SQLAlchemy 모델 정의
│   │   └── database.py           # DB 연결 및 세션 관리
│   │
│   └── utils/                    # 공통 유틸리티 함수
│       ├── __init__.py
│       └── helpers.py
│
├── scripts/                      # 독립 실행용 스크립트 모음
│   ├── setup_database.py         # 초기 DB 설정 스크립트
│   └── run_crawler.py            # 크롤러 실행 스크립트
│
├── config/                       # 환경 및 로깅 설정
│   └── logging_config.py
│
├── logs/                         # 실행 로그 저장 디렉토리
│
├── .env.template                 # 환경변수 템플릿 파일 (.env 참고용)
├── .gitignore                    # Git 무시 규칙 설정
├── requirements.txt              # 의존성 패키지 목록
└── README.md                     # 프로젝트 설명 문서

```

---

### ✅ 특징 요약

| 폴더/파일 | 용도 |
| --- | --- |
| `src/crawler/` | 마라톤 사이트를 크롤링하고 HTML 파싱하는 핵심 로직 |
| `src/database/` | DB 모델 정의 및 세션 관리 (`SQLAlchemy` 기반) |
| `scripts/` | DB 설정 및 크롤러 실행을 위한 별도 실행 스크립트 |
| `logs/` | 실행 로그 저장 위치 (`volume`으로 외부 마운트 가능) |
| `.env.template` | 환경 변수 구성 예시 제공 (배포용 `.env` 별도) |
| `requirements.txt` | Python 패키지 관리 (`pip install -r`) |

## 📄 프로젝트 구조 - `spring-boot`

```
spring-boot/
│
├── src/main/
│   ├── java/com/runhwani/runmate/
│   │   ├── config/           # 전역 설정 클래스
│   │   ├── controller/       # API 엔드포인트
│   │   ├── dao/              # DAO 레이어
│   │   ├── dto/              # 요청/응답 DTO
│   │   ├── exception/        # 예외 처리 클래스
│   │   ├── interceptor/      # 요청 인터셉터
│   │   ├── model/            # JPA/DB 모델
│   │   ├── mybatis/          # MyBatis 설정 및 관련 클래스
│   │   ├── security/         # 보안 설정 (JWT, 필터 등)
│   │   ├── service/          # 비즈니스 로직 처리
│   │   ├── utils/            # 공통 유틸리티 클래스
│   │   └── RunmateApplication.java  # 메인 실행 클래스
│
├── resources/
│   ├── mappers/              # MyBatis 매퍼 XML
│   ├── application.yml       # 기본 설정
│   ├── application-dev.yml   # 개발 환경 설정
│   ├── application-prod.yml  # 운영 환경 설정
│   └── runmate-*-firebase-adminsdk-*.json  # Firebase 서비스 계정 키
│
├── .env                      # 환경변수 파일
├── build.gradle              # Gradle 빌드 설정
├── gradlew / gradlew.bat     # Gradle Wrapper
└── README.md                 # 프로젝트 설명서

```

---

### ✅ 패키지 설명

| 패키지/디렉토리 | 설명 |
| --- | --- |
| `controller/` | API 엔드포인트 정의  |
| `service/` | 핵심 비즈니스 로직 처리 |
| `dao/` | DB 접근용 DAO 클래스 (MyBatis 또는 직접 SQL) |
| `dto/` | 계층 간 데이터 전달 객체 |
| `model/` | DB 모델 혹은 JPA 엔티티 |
| `mybatis/` | MyBatis 설정 클래스 또는 매퍼 인터페이스 |
| `exception/` | 사용자 정의 예외 및 예외 핸들러 |
| `security/` | JWT 토큰 인증 및 보안 관련 설정 |
| `interceptor/` | 요청 사전/후처리를 위한 Spring 인터셉터 |
| `utils/` | 공통으로 재사용되는 헬퍼 함수 등 |
| `config/` | 전역 설정 클래스 (CORS, Swagger, WebMvc 등) |

---

API 문서 (Swagger)

- **Swagger UI:** https://k12d107.p.ssafy.io/swagger-ui/index.html
- **OpenAPI JSON:** https://k12d107.p.ssafy.io/v3/api-docs

---

## 📄 03. 환경 변수 (.env)

### 📁 설정 위치

- 프로젝트 루트 디렉토리에 `.env` 파일을 생성합니다.

### ⚙️ `.env` 파일

```
# Database
DB_HOST=k12d107.p.ssafy.io
DB_PORT=5432
DB_NAME=d107
DB_USERNAME=runhwani
DB_PASSWORD=********

# Redis
REDIS_HOST=k12d107.p.ssafy.io
REDIS_PORT=6379

# App URL
APP_BASE_URL=https://k12d107.p.ssafy.io
# APP_BASE_URL=http://localhost:8080

# OpenAI
OPENAI_API_KEY=sk-**************************************

# 서버 포트
PORT=3000
CORS_ORIGIN=*

# 크롤링 설정
PAGES_TO_CRAWL=5
CRAWL_DELAY_MIN=1.0
CRAWL_DELAY_MAX=3.0

# 스케줄러 설정
CRAWL_HOUR=1
CRAWL_MINUTE=0
RUN_ON_START=false

# Firebase (환경 변수 기반 키 관리 권장)
FIREBASE_PRIVATE_KEY_ID=***************
FIREBASE_PRIVATE_KEY=-----BEGIN PRIVATE KEY-----\\nMIIE...\\n-----END PRIVATE KEY-----\\n

```

> 🔐 DB_PASSWORD, OPENAI_API_KEY, FIREBASE_PRIVATE_KEY 등 민감 정보는 비공개 환경에서만 관리
> 

---

### 📄 application.yml

```yaml
spring:
  config:
    import: optional:file:.env[.properties]

  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      connection-init-sql: SET TIME ZONE 'Asia/Seoul'
      connection-timeout: 30000
      maximum-pool-size: 10
      minimum-idle: 5
      connection-test-query: SELECT 1

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      timeout: 10000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1
      connect-timeout: 10000

  servlet:
    multipart:
      enabled: true
      max-file-size: 20MB
      max-request-size: 20MB
      resolve-lazily: true

  mvc:
    converters:
      preferred-json-mapper: jackson

  jackson:
    time-zone: Asia/Seoul

mybatis:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  mapper-locations: classpath:mappers/*.xml
  type-aliases-package: com.runhwani.runmate.model

management:
  endpoints:
    web:
      exposure:
        include: health,info

jwt:
  secret: ${JWT_SECRET:your-default-jwt-secret-key-should-be-very-long-and-secure}
  token-validity-in-seconds: 86400

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    disable-swagger-default-url: true
    enabled: true
    config-url: /v3/api-docs/swagger-config
    urls:
      - url: /v3/api-docs
        name: API Docs
  api-docs:
    path: /v3/api-docs
  show-actuator: false
  default-produces-media-type: application/json
  default-consumes-media-type: application/json

logging:
  level:
    root: INFO
    com.runhwani.runmate: DEBUG
  pattern:
    dateformat: yyyy-MM-dd HH:mm:ss.SSS,Asia/Seoul

server:
  port: 8080

file:
  upload-dir: ./uploads

app:
  base-url: ${APP_BASE_URL:https://k12d107.p.ssafy.io}

openai:
  api-key: ${OPENAI_API_KEY}

firebase:
  config-file: classpath:runmate-e11b0-firebase-adminsdk-fbsvc-358767a133.json
  notification:
    default-topic: all-users
    default-icon: notification_icon
    default-color: "#4CAF50"

```

---

## 📄 04. CI/CD 구축

### ✅ AWS EC2 접속

```bash
# .pem 키가 있는 디렉토리에서 실행
ssh -i j12D107T.pem ubuntu@j12D107.p.ssafy.io
```

> ※ .pem 권한 설정 필요시
> 
> 
> `chmod 400 j12D107T.pem`
> 

---

### **Docker-compose.yml**

```bash
version: '3'

services:
  springboot:
    image: openjdk:17-jdk-slim
    build: .
    container_name: runmate-backend
    volumes:
      - ./app.jar:/app/app.jar # 호스트의 app.jar를 컨테이너에 마운트
      - runmate-logs:/app/logs # 로그 데이터를 위한 볼륨 추가
      - runmate-gpx:/app/gpx # GPX 데이터를 위한 볼륨 추가
      - runmate-uploads:/app/uploads # 업로드 파일을 위한 볼륨 추가
      - /etc/localtime:/etc/localtime:ro # 호스트의 시간대 설정을 컨테이너에 적용
      - /etc/timezone:/etc/timezone:ro
    restart: always
    ports:
      - '8080:8080'
    command: ['java', '-jar', '/app/app.jar']
    env_file:
      - .env
    environment:
      - APP_BASE_URL=https://k12d107.p.ssafy.io # 기본 URL 설정
      - JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Seoul # JVM 시간대 설정
      - TZ=Asia/Seoul # 컨테이너 레벨 TZ 환경변수도 함께 지정
    depends_on:
      - postgres
      - redis
    networks:
      - project-net

  postgres:
    image: postgres:15
    container_name: postgres
    restart: always
    env_file:
      - .env
    environment:
      # 기본 슈퍼유저로 initdb 수행
      # POSTGRES_USER, POSTGRES_DB 는 지정하지 않습니다.
      - POSTGRES_PASSWORD=${DB_PASSWORD}
      - TZ=Asia/Seoul
    volumes:
      # initdb 후 init-user-db.sh 를 실행
      - ./postgres-init:/docker-entrypoint-initdb.d
      - postgres-data:/var/lib/postgresql/data # 데이터베이스 데이터를 위한 볼륨 추가
      - /etc/localtime:/etc/localtime:ro
      - /etc/timezone:/etc/timezone:ro
    networks:
      - project-net
    ports:
      - '5432:5432'

  redis:
    image: redis:7
    container_name: redis
    restart: always
    ports:
      - '6379:6379'
    volumes:
      - redis-data:/data # Redis 데이터를 위한 볼륨 추가
    command: redis-server --appendonly yes # AOF 지속성 활성화
    networks:
      - project-net

  nodejs:
    build: ./backend/nodejs
    container_name: runmate-realtime
    restart: always
    ports:
      - '3000:3000'
    volumes:
      - runmate-logs:/app/logs
    env_file:
      - .env
    environment:
      - PORT=3000
      - CORS_ORIGIN=*
    networks:
      - project-net

  nginx:
    image: nginx:stable
    container_name: nginx
    restart: always
    ports:
      - '80:80'
      - '443:443'
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/conf.d/default.conf
      - certbot-etc:/etc/letsencrypt
      - web-root:/var/www/html
      - nginx-logs:/var/log/nginx # Nginx 로그를 위한 볼륨 추가
      - runmate-uploads:/app/uploads:ro # 업로드 파일을 읽기 전용으로 마운트
    depends_on:
      - springboot
    networks:
      - project-net

  certbot:
    image: certbot/certbot
    container_name: certbot
    volumes:
      - certbot-etc:/etc/letsencrypt
      - certbot-var:/var/lib/letsencrypt
      - ./nginx/nginx.conf:/etc/nginx/conf.d/default.conf
      - web-root:/var/www/html
    command: certonly --webroot --webroot-path=/var/www/html --email kimh0414@gmail.com --agree-tos --no-eff-email -d k12d107.p.ssafy.io
    depends_on:
      - nginx

  marathon-crawler:
    build:
      context: ./backend/marathon-crawler
      dockerfile: Dockerfile
    container_name: marathon-crawler
    restart: always
    volumes:
      - ./backend/marathon-crawler/logs:/app/logs
      - ./backend/marathon-crawler/.env:/app/.env
      - runmate-logs:/app/logs/shared # 공유 로그 볼륨 추가
    env_file:
      - .env # 공통 환경 변수 파일 사용
    environment:
      - DB_USERNAME=${DB_USERNAME}
      - DB_PASSWORD=${DB_PASSWORD}
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=${DB_NAME}
      - CRAWL_HOUR=1
      - CRAWL_MINUTE=0
      - RUN_ON_START=false
      - LOG_LEVEL=INFO
    depends_on:
      - postgres
    networks:
      - project-net # runmate-network에서 project-net으로 변경

networks:
  project-net:
    driver: bridge
  runmate-network:
    driver: bridge

volumes:
  certbot-etc:
  certbot-var:
  web-root:
  postgres-data: # PostgreSQL 데이터 볼륨 정의
  redis-data: # Redis 데이터 볼륨 정의
  runmate-logs: # 애플리케이션 로그 볼륨 정의
  nginx-logs: # Nginx 로그 볼륨 정의
  runmate-gpx: # GPX 데이터 볼륨 정의
  runmate-uploads: # 업로드 파일 볼륨 정의

```

### **Docker file**

backend

```bash
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

```

node.js

```bash
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

```

marathon-crawler

```bash
FROM python:3.9-slim

WORKDIR /app

# 필요한 패키지 설치
RUN apt-get update && \
    apt-get install -y --no-install-recommends gcc python3-dev libpq-dev && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 의존성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# 로그 디렉토리 생성
RUN mkdir -p logs

# 실행 권한 부여
RUN chmod +x app.py

# 환경 변수 설정
ENV PYTHONUNBUFFERED=1

# 애플리케이션 실행
CMD ["python", "app.py"] 
```

jenkinsfile

```bash
pipeline {
    agent any

    environment {
        REMOTE    = 'ubuntu@k12d107.p.ssafy.io'
        APPDIR    = '/home/ubuntu/runmate-backend'
        JAR_NAME  = 'app.jar'
    }

    stages {
        stage('Checkout') {
            steps {
                git(
                    url: 'https://lab.ssafy.com/s12-final/S12P31D107.git',
                    branch: 'develop/back',
                    credentialsId: 'gitlab-https'
                )
            }
        }

        stage('Grant Permissions') {
            steps {
                // gradlew 실행 권한 추가
                sh 'chmod +x backend/spring-boot/gradlew'
            }
        }

        stage('Build JAR') {
            steps {
                // spring-boot 디렉토리로 이동해서 빌드
                sh 'cd backend/spring-boot && ./gradlew clean bootJar -x test'
            }
        }

        stage('Build Node.js') {
            steps {
                // Node.js 애플리케이션 의존성 설치 및 빌드
                sh '''
                    cd backend/nodejs
                    npm ci
                    npm run build
                '''
            }
        }

        stage('Prepare Marathon Crawler') {
            steps {
                // 마라톤 크롤러 디렉토리 생성 및 필요한 파일 준비
                sh '''
                    mkdir -p backend/marathon-crawler/logs
                    touch backend/marathon-crawler/.env
                '''
            }
        }

        stage('Deploy to EC2') {
            steps {
                sshagent(['ec2-ssh']) {
                    // 빌드된 JAR 파일을 EC2로 전송
                    sh """
                        scp -o StrictHostKeyChecking=no backend/spring-boot/build/libs/*.jar \
                            ${REMOTE}:${APPDIR}/${JAR_NAME}
                    """
                    
                    // Node.js 애플리케이션 디렉토리 복사
                    sh """
                        ssh -o StrictHostKeyChecking=no ${REMOTE} 'mkdir -p ${APPDIR}/backend/nodejs'
                        
                        scp -o StrictHostKeyChecking=no -r backend/nodejs/package*.json \
                            backend/nodejs/tsconfig.json \
                            backend/nodejs/src \
                            backend/nodejs/Dockerfile \
                            ${REMOTE}:${APPDIR}/backend/nodejs/
                    """
                    
                    // 마라톤 크롤러 디렉토리 복사
                    sh """
                        ssh -o StrictHostKeyChecking=no ${REMOTE} 'mkdir -p ${APPDIR}/backend/marathon-crawler/logs'
                        
                        scp -o StrictHostKeyChecking=no -r backend/marathon-crawler/Dockerfile \
                            backend/marathon-crawler/requirements.txt \
                            backend/marathon-crawler/app.py \
                            backend/marathon-crawler/src \
                            backend/marathon-crawler/config \
                            backend/marathon-crawler/scripts \
                            backend/marathon-crawler/.env \
                            ${REMOTE}:${APPDIR}/backend/marathon-crawler/
                    """
                    
                    // docker-compose 재시작
                    sh """
                        ssh -o StrictHostKeyChecking=no ${REMOTE} \\
                            'cd ${APPDIR} &&
                             echo "Docker 컨테이너 중지 및 제거" &&
                             docker-compose down --remove-orphans &&
                             echo "Node.js 이미지 재빌드" &&
                             docker-compose build --no-cache nodejs &&
                             echo "새 컨테이너 시작" &&
                             docker-compose up -d
                            '
                    """
                }
            }
        }

        stage('Notification') {
            steps {
                echo 'jenkins notification!'
            }
            post {
                success {
                    script {
                        def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                        def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                        mattermostSend(color: 'good',
                            message: "빌드 성공: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)",
                            endpoint: 'https://meeting.ssafy.com/hooks/zcfm5oym4pdybkp9fbjia331rw',
                            channel: 'D107git'
                        )
                    }
                }
                failure {
                    script {
                        def Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                        def Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
                        mattermostSend(color: 'danger',
                            message: "빌드 실패: ${env.JOB_NAME} #${env.BUILD_NUMBER} by ${Author_ID}(${Author_Name})\n(<${env.BUILD_URL}|Details>)",
                            endpoint: 'https://meeting.ssafy.com/hooks/zcfm5oym4pdybkp9fbjia331rw',
                            channel: 'D107git'
                        )
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ Deploy succeeded'
        }
        failure {
            echo '❌ Deploy failed'
        }
    }
}

```

Nginx.config

```bash
upstream backend {
    server runmate-backend:8080;
}

upstream socketio {
    server runmate-realtime:3000;
}

server {
    listen 80;
    server_name k12d107.p.ssafy.io;
    
    location /.well-known/acme-challenge/ {
        root /var/www/html;
    }

    location / {
        return 301 https://$host$request_uri;
    }
}

# 3000 포트로 들어오는 HTTP 요청을 HTTPS로 리다이렉트
server {
    listen 3000;
    server_name k12d107.p.ssafy.io;
    
    location / {
        return 301 https://$host/socket.io/;
    }
}

server {
    listen 443 ssl;
    server_name k12d107.p.ssafy.io;

    ssl_certificate     /etc/letsencrypt/live/k12d107.p.ssafy.io/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/k12d107.p.ssafy.io/privkey.pem;
    
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;

    # 보안 헤더 추가
    add_header Cache-Control "no-cache,no-store,max-age=0,must-revalidate";
    add_header Pragma "no-cache";
    add_header Expires "0";
    add_header X-Frame-Options "DENY";
    add_header X-Content-Type-Options "nosniff";
    add_header X-XSS-Protection "0";
    
    # CORS 헤더 설정
    add_header Vary "Origin,Access-Control-Request-Method,Access-Control-Request-Headers";

    # CORS 설정
    add_header 'Access-Control-Allow-Origin' '*' always;
    add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS, PUT, DELETE' always;
    add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization' always;

    # Socket.IO 서버 프록시 설정
    location /socket.io/ {
        # 디버깅을 위한 로그 추가
        error_log /var/log/nginx/socketio-error.log debug;
        access_log /var/log/nginx/socketio-access.log;
        
        proxy_pass http://socketio;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # WebSocket 연결 유지를 위한 타임아웃 설정
        proxy_connect_timeout 7d;
        proxy_send_timeout 7d;
        proxy_read_timeout 7d;
        
        # CORS 설정
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS' always;
        add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization' always;
        
        # OPTIONS 요청 처리
        if ($request_method = 'OPTIONS') {
            add_header 'Access-Control-Allow-Origin' '*';
            add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS';
            add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization';
            add_header 'Access-Control-Max-Age' 1728000;
            add_header 'Content-Type' 'text/plain charset=UTF-8';
            add_header 'Content-Length' 0;
            return 204;
        }
        
        # 403 오류 방지를 위한 설정
        proxy_set_header Origin "";
        proxy_hide_header X-Frame-Options;
        proxy_intercept_errors off;
    }

    # 클라이언트 요청 크기 제한 증가
    client_max_body_size 20M;

    location / {
        # CORS 설정
        add_header 'Access-Control-Allow-Origin' '*';
        add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS, PUT, DELETE';
        add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization';
        
        if ($request_method = 'OPTIONS') {
            add_header 'Access-Control-Allow-Origin' '*';
            add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS, PUT, DELETE';
            add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization';
            add_header 'Access-Control-Max-Age' 1728000;
            add_header 'Content-Type' 'text/plain charset=UTF-8';
            add_header 'Content-Length' 0;
            return 204;
        }

        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
        proxy_read_timeout 300;
        
        proxy_buffer_size          128k;
        proxy_buffers             4 256k;
        proxy_busy_buffers_size    256k;
        
        # 디버깅을 위한 에러 로그 설정
        error_log /var/log/nginx/error.log debug;
        access_log /var/log/nginx/access.log;
    }

    location /api {
        if ($request_method = 'OPTIONS') {
            add_header 'Access-Control-Allow-Origin' '*' always;
            add_header 'Access-Control-Allow-Methods' 'GET, POST, OPTIONS, PUT, DELETE' always;
            add_header 'Access-Control-Allow-Headers' 'DNT,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Range,Authorization' always;
            add_header 'Access-Control-Max-Age' 1728000;
            add_header 'Content-Type' 'text/plain charset=UTF-8';
            add_header 'Content-Length' 0;
            return 204;
        }

        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /swagger-ui {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 업로드된 파일에 대한 프록시 설정 추가
    location /uploads/ {
        proxy_pass http://backend/uploads/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 캐싱 설정 (선택사항)
        expires 30d;
        add_header Cache-Control "public, max-age=2592000";
        
        # CORS 설정
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, OPTIONS' always;
        
        # 큰 파일 전송을 위한 설정
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
        proxy_read_timeout 300;
        proxy_buffering on;
        proxy_buffer_size 128k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
    }
    
    # GPX 파일에 대한 프록시 설정 추가
    location /gpx/ {
        proxy_pass http://backend/gpx/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # 캐싱 설정 (선택사항)
        expires 30d;
        add_header Cache-Control "public, max-age=2592000";
        
        # CORS 설정
        add_header 'Access-Control-Allow-Origin' '*' always;
        add_header 'Access-Control-Allow-Methods' 'GET, OPTIONS' always;
        
        # 큰 파일 전송을 위한 설정
        proxy_connect_timeout 300;
        proxy_send_timeout 300;
        proxy_read_timeout 300;
        proxy_buffering on;
        proxy_buffer_size 128k;
        proxy_buffers 4 256k;
        proxy_busy_buffers_size 256k;
    }
}

```

---

## 📄 05. 외부 서비스 사용

### ✅ 외부 API 및 연동 서비스

| 서비스명 | 용도 및 설명 |
| --- | --- |
| **FCM** (Firebase Cloud Messaging) | 앱 푸시 알림 전송에 사용Firebase Admin SDK 연동 |
| **Mattermost** | Jenkins 빌드 결과를 실시간 알림으로 전송Webhook 기반 |
| **Kakao 지도 API** | 위치 기반 기능 구현에 사용지도 표시, 주소-좌표 변환 등 제공 |

### 🔥 Firebase

| 항목             | 내용                                                          |
| -------------- | ----------------------------------------------------------- |
| **JSON 파일 위치** | `src/main/resources/runmate-xxx-firebase-adminsdk-xxx.json` |
| **설정 경로**      | `application.yml > firebase.config-file`                    |
| **초기화 코드 위치**  | `FirebaseConfig.java`        |
| **사용 목적**      | FCM 푸시 알림 발송               |




