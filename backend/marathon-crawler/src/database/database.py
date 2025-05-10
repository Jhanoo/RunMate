"""
데이터베이스 연결 및 세션 관리
"""
import os
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, scoped_session
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

# 데이터베이스 연결 정보
DB_USERNAME = os.getenv('DB_USERNAME')
DB_PASSWORD = os.getenv('DB_PASSWORD')
DB_HOST = os.getenv('DB_HOST')
DB_PORT = os.getenv('DB_PORT')
DB_NAME = os.getenv('DB_NAME')

# 데이터베이스 URL 생성 (PostgreSQL)
DATABASE_URL = f"postgresql://{DB_USERNAME}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}"

# SQLAlchemy 엔진 생성 (echo=True는 로깅을 활성화합니다)
engine = create_engine(DATABASE_URL, echo=False)

# 세션 팩토리 생성
session_factory = sessionmaker(bind=engine)

# 스레드 안전한 세션 생성
Session = scoped_session(session_factory)

def get_db_session():
    """새로운 데이터베이스 세션 반환"""
    return Session()

def close_db_session(session):
    """데이터베이스 세션 닫기"""
    session.close()