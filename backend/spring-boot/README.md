# RunMate 백엔드 서버

마라톤 도우미 앱 “RunMate”의 백엔드(Spring Boot) 서버입니다.  
PostgreSQL, MyBatis, Redis를 사용하며, RESTful API 및 Swagger 문서화를 제공합니다.

---

## 목차

- [RunMate 백엔드 서버](#runmate-백엔드-서버)
  - [목차](#목차)
  - [프로젝트 소개](#프로젝트-소개)
  - [기술 스택](#기술-스택)
  - [사전 요구사항](#사전-요구사항)
  - [환경 변수 설정](#환경-변수-설정)
  - [API 문서 (Swagger)](#api-문서-swagger)
  - [프로젝트 구조](#프로젝트-구조)

---

## 프로젝트 소개

- “RunMate” 마라톤 도우미 앱의 백엔드 서버  
- 사용자의 러닝 데이터(페이스, 심박수, 경로 등)를 저장·분석하고,  
  개인 맞춤형 훈련 커리큘럼 및 그룹 러닝 기능을 제공합니다.

---

## 기술 스택

| 구분           | 기술／라이브러리           |
| ------------- | -------------------------- |
| Language      | Java 17                    |
| Framework     | Spring Boot 3.4.5          |
| DB            | PostgreSQL                 |
| ORM           | MyBatis                    |
| Cache / Queue | Redis                      |
| API 문서화    | Springdoc OpenAPI (Swagger)|
| 빌드 도구      | Gradle (Groovy)        |
| CI/CD         | Jenkins, Docker            |

---

## 사전 요구사항

- Java 17 이상  
- Gradle 8.10  
- PostgreSQL 서버  
- Redis 서버  
- Git

---

## 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하고, 아래 값을 설정하세요:

```dotenv
# PostgreSQL Database
DB_HOST=example.k12d107.p.ssafy.io
DB_PORT=5432
DB_NAME=exampleDB
DB_USERNAME=testId
DB_PASSWORD=testPw

# Redis
REDIS_HOST=example.k12d107.p.ssafy.io
REDIS_PORT=6379

# OpenAI API Key
OPENAI_API_KEY= # YOUR_API_KEY
```

## API 문서 (Swagger)
- Swagger UI: https://k12d107.p.ssafy.io/swagger-ui/index.html
- OpenAPI JSON: https://k12d107.p.ssafy.io/v3/api-docs

## 프로젝트 구조
```bash
runmate-backend/
├─ src/
│ └─ main/
│   ├─ java/com/runhwani/runmate
│   │ ├─ config # 각종 설정 (Redis, Swagger, Security 등)
│   │ ├─ controller # REST API 컨트롤러
│   │ ├─ dao # MyBatis DAO (Mapper 인터페이스)
│   │ ├─ dto # 요청/응답용 DTO
│   │ ├─ exception # 예외 처리 및 커스텀 예외 클래스
│   │ ├─ model # 도메인 모델 (엔티티)
│   │ ├─ mybatis # MyBatis 설정 및 매퍼 XML 연동용 클래스
│   │ ├─ security/jwt # JWT 보안 관련 클래스
│   │ ├─ service # 비즈니스 로직
│   │ ├─ utils # 유틸리티 클래스
│   │ └─ RunmateApplication.java
│   └─ resources/
│     ├─ mappers # MyBatis XML 매퍼 파일
│     ├─ application.yml
│     ├─ application-dev.yml
│     └─ application-prod.yml
├─ .env # 환경변수 파일 (Git에 커밋 금지)
├─ build.gradle
├─ gradlew
├─ gradlew.bat
├─ README.md
└─ settings.gradle
```
