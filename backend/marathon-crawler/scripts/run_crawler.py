#!/usr/bin/env python
"""
마라톤 크롤러 실행 스크립트
"""
import os
import sys
import logging
import time
import codecs
from dotenv import load_dotenv

# 현재 스크립트 경로를 기준으로 프로젝트 루트 경로 설정
project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, project_root)

# Windows 환경에서 출력 스트림 인코딩 설정
if sys.stdout.encoding != 'utf-8':
    sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')
    sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer, 'strict')

# 로그 디렉터리 생성
logs_dir = os.path.join(project_root, 'logs')
os.makedirs(logs_dir, exist_ok=True)

# .env 파일 로드
load_dotenv()

# 로그 레벨 설정
log_level_str = os.getenv('LOG_LEVEL', 'INFO')
log_level = getattr(logging, log_level_str.upper(), logging.INFO)

# 로깅 설정
logging.basicConfig(
    level=log_level,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(os.path.join(logs_dir, 'marathon_crawler.log'), encoding='utf-8'),
        logging.StreamHandler()
    ]
)

from src.crawler.marathon_crawler import MarathonCrawler

def main():
    """크롤러 실행"""
    start_time = time.time()
    
    try:
        # 크롤러 실행
        with MarathonCrawler() as crawler:
            crawler.crawl()
        
        # 실행 시간 측정
        elapsed_time = time.time() - start_time
        logging.info(f"크롤링 완료. 소요 시간: {elapsed_time:.2f}초")
        
    except Exception as e:
        logging.error(f"크롤러 실행 중 오류 발생: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()