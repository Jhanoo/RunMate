import os
import psycopg2
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

# 데이터베이스 연결 정보
DB_USERNAME = os.getenv('DB_USERNAME')
DB_PASSWORD = os.getenv('DB_PASSWORD')
DB_HOST = os.getenv('DB_HOST')
DB_PORT = os.getenv('DB_PORT', '5432')
DB_NAME = os.getenv('DB_NAME')

def create_tables():
    """데이터베이스 테이블 생성"""
    # DB 연결
    conn = psycopg2.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USERNAME,
        password=DB_PASSWORD,
        dbname=DB_NAME
    )
    
    try:
        with conn.cursor() as cur:
            # 확장 모듈 활성화 (UUID 생성 함수 사용을 위해)
            cur.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto;")
            
            # 마라톤 테이블 생성
            cur.execute("""
            CREATE TABLE IF NOT EXISTS marathons (
                marathon_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                name VARCHAR(255) NOT NULL,
                date TIMESTAMPTZ NOT NULL,
                location VARCHAR(300) NOT NULL,
                created_at TIMESTAMPTZ NOT NULL DEFAULT now()
            );
            """)
            
            # 마라톤 거리 테이블 생성
            cur.execute("""
            CREATE TABLE IF NOT EXISTS marathon_distances (
                distance_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                marathon_id UUID NOT NULL REFERENCES marathons(marathon_id),
                distance VARCHAR(30) NOT NULL
            );
            """)
            
            # 인덱스 생성
            cur.execute("""
            CREATE INDEX IF NOT EXISTS idx_marathons_name_date ON marathons (name, date);
            CREATE INDEX IF NOT EXISTS idx_marathon_distances_marathon_id ON marathon_distances (marathon_id);
            """)
            
            conn.commit()
            print("데이터베이스 테이블 생성 완료")
            
    except Exception as e:
        conn.rollback()
        print(f"테이블 생성 중 오류 발생: {str(e)}")
    
    finally:
        conn.close()

if __name__ == "__main__":
    create_tables()
    print("데이터베이스 설정이 완료되었습니다. 이제 크롤러를 실행할 수 있습니다.")