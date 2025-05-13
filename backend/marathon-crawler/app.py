#!/usr/bin/env python
"""
메인 애플리케이션 모듈: 스케줄러와 필요한 서비스 시작
"""
import os
import sys
import time
import signal
from dotenv import load_dotenv

# 현재 스크립트 경로를 기준으로 프로젝트 루트 경로 설정
project_root = os.path.abspath(os.path.dirname(__file__))
sys.path.insert(0, project_root)

# logging 설정 모듈 import 및 설정
from config.logging_config import setup_logging
logger = setup_logging(log_dir=os.path.join(project_root, 'logs'))

# .env 파일 로드
load_dotenv()

from src.scheduler.scheduler import start_scheduler

# 종료 플래그
should_exit = False

def signal_handler(sig, frame):
    """시그널 핸들러: 프로그램 종료 처리"""
    global should_exit
    logger.info("종료 시그널 수신, 애플리케이션을 종료합니다...")
    should_exit = True

def main():
    """메인 함수"""
    logger.info("마라톤 크롤링 애플리케이션 시작")

    # 종료 시그널 핸들러 등록
    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)

    scheduler = start_scheduler()
    if not scheduler:
        logger.error("스케줄러 시작 실패, 애플리케이션을 종료합니다.")
        return

    run_on_start = os.getenv('RUN_ON_START', 'false').lower() == 'true'
    if run_on_start:
        logger.info("시작 시 크롤링 작업 실행")
        from src.scheduler.scheduler import crawl_marathons
        crawl_marathons()

    try:
        while not should_exit:
            time.sleep(1)
    except KeyboardInterrupt:
        logger.info("사용자 요청으로 애플리케이션을 종료합니다.")
    finally:
        if scheduler and scheduler.running:
            scheduler.shutdown()
            logger.info("스케줄러 종료")
        
        logger.info("마라톤 크롤링 애플리케이션 종료")

if __name__ == "__main__":
    main()
