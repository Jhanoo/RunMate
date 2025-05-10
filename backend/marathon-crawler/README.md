# 마라톤 정보 크롤러

이 프로젝트는 SUB:PACE(subpace.app) 웹사이트에서 마라톤 이벤트 정보를 수집하여 PostgreSQL 데이터베이스에 저장하는 웹 크롤러입니다.

## 프로젝트 구조

```
marathon-crawler/
│
├── src/                          # 소스 코드 디렉토리
│   ├── crawler/                  # 크롤러 관련 모듈
│   │   ├── __init__.py
│   │   ├── marathon_crawler.py   # 메인 크롤링 로직
│   │   └── parser.py            # HTML 파싱 함수들
│   │
│   ├── database/                 # 데이터베이스 관련 모듈
│   │   ├── __init__.py
│   │   ├── models.py            # SQLAlchemy 모델 정의
│   │   └── database.py          # DB 연결 및 세션 관리
│   │
│   └── utils/                    # 유틸리티 모듈
│       ├── __init__.py
│       └── helpers.py           # 공통 유틸리티 함수
│
├── scripts/                      # 스크립트 디렉토리
│   ├── setup_database.py        # DB 설정 스크립트
│   └── run_crawler.py           # 크롤러 실행 스크립트
│
├── config/                       # 설정 관련 디렉토리 
│   └── logging_config.py        # 로깅 설정
│
├── logs/                         # 로그 디렉토리
│
├── .env.template                 # 환경 변수 템플릿
├── .gitignore                    # Git 무시 파일 목록
├── requirements.txt              # 패키지 의존성
└── README.md                     # 프로젝트 설명서
```

## 주요 변경 사항

- marathon365.net 대신 SUB:PACE(subpace.app) 웹사이트에서 크롤링하도록 변경
- 웹사이트 구조에 맞게 파싱 로직 수정
- API 기반 데이터 추출 로직 추가

## 데이터베이스 구조

- **marathons**: 마라톤 기본 정보
  - `marathon_id`: UUID 기본 키
  - `name`: 마라톤 이름
  - `date`: 개최 날짜/시간 (시간대 포함)
  - `location`: 개최 장소
  - `created_at`: 레코드 생성 시간

- **marathon_distances**: 마라톤 거리 정보
  - `distance_id`: UUID 기본 키
  - `marathon_id`: 마라톤 참조 키
  - `distance`: 거리/종목 정보

## 설치 방법

1. 프로젝트 클론:
```bash
git clone <repository-url>
cd marathon-crawler
```

2. 가상 환경 생성 및 활성화:
```bash
python -m venv venv
source venv/bin/activate  # Linux/Mac
# 또는
venv\Scripts\activate     # Windows
```

3. 필요한 라이브러리 설치:
```bash
pip install -r requirements.txt
```

4. `.env` 파일 설정:
```bash
cp .env.template .env
# .env 파일을 편집하여 데이터베이스 접속 정보를 입력
```

5. 로그 디렉토리 생성:
```bash
mkdir -p logs
```

6. 데이터베이스 테이블 생성:
```bash
python scripts/setup_database.py
```

## 스케줄러 사용 방법

매일 정해진 시간에 자동으로 마라톤 정보를 크롤링하는 스케줄러가 포함되어 있습니다.

1. 환경 변수 설정:
```
CRAWL_HOUR=1       # 크롤링 실행 시간 (24시간 형식, 0-23)
CRAWL_MINUTE=0     # 크롤링 실행 분 (0-59)
RUN_ON_START=false # 애플리케이션 시작 시 즉시 크롤링 실행 여부
```

2. 애플리케이션 실행:
```bash
python app.py
```

3. Docker로 실행:
```bash
docker build -t marathon-crawler .
docker run -d --name marathon-crawler \
    -v $(pwd)/logs:/app/logs \
    -v $(pwd)/.env:/app/.env \
    marathon-crawler
```

애플리케이션이 실행되면 지정된 시간(기본값: 매일 01:00)에 자동으로 마라톤 정보를 크롤링하여 데이터베이스에 저장합니다.

## 환경 변수 설정

`.env` 파일에서 다음 설정을 구성할 수 있습니다:

- **DB_USERNAME**: 데이터베이스 사용자 이름
- **DB_PASSWORD**: 데이터베이스 비밀번호
- **DB_HOST**: 데이터베이스 호스트 (기본값: localhost)
- **DB_PORT**: 데이터베이스 포트 (기본값: 5432)
- **DB_NAME**: 데이터베이스 이름

- **PAGES_TO_CRAWL**: 크롤링할 페이지 수 (기본값: 1)
- **CRAWL_DELAY_MIN**: 최소 요청 간격 (초) (기본값: 1.0)
- **CRAWL_DELAY_MAX**: 최대 요청 간격 (초) (기본값: 3.0)
- **LOG_LEVEL**: 로깅 레벨 (기본값: INFO)

## 주의사항

- 웹 크롤링 시 해당 웹사이트의 이용약관을 준수하세요.
- 과도한 요청은 서버에 부담을 줄 수 있으므로 적절한 간격을 유지하세요.
- 수집한 데이터를 상업적으로 이용할 경우 관련 법규를 확인하세요.