#!/usr/bin/env python
"""
마라톤 크롤러 실행 스크립트
"""
import os
import sys
import time
from dotenv import load_dotenv

# 현재 스크립트 경로를 기준으로 프로젝트 루트 경로 설정
project_root = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
sys.path.insert(0, project_root)

# logging 설정 모듈 import 및 설정
from config.logging_config import setup_logging
logger = setup_logging(log_dir=os.path.join(project_root, 'logs'))

# .env 파일 로드
load_dotenv()

from src.crawler.marathon_crawler import MarathonCrawler

def main():
    """크롤러 실행"""
    start_time = time.time()

    try:
        # 크롤러 실행
        with MarathonCrawler() as crawler:
            crawler.crawl()
        
        elapsed_time = time.time() - start_time
        logger.info(f"크롤링 완료. 소요 시간: {elapsed_time:.2f}초")
    
    except Exception as e:
        logger.error(f"크롤러 실행 중 오류 발생: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()
