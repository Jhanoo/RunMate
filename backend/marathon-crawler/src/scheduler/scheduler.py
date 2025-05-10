"""
스케줄러 모듈: 마라톤 크롤링 작업을 정해진 시간에 실행
"""
import os
import logging
from datetime import datetime
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
from dotenv import load_dotenv

from src.crawler.marathon_crawler import MarathonCrawler

# 로거 설정
logger = logging.getLogger(__name__)

# .env 파일 로드
load_dotenv()

# 환경 변수에서 스케줄링 시간 로드
CRAWL_HOUR = int(os.getenv('CRAWL_HOUR', '1'))     # 기본값 01:00
CRAWL_MINUTE = int(os.getenv('CRAWL_MINUTE', '0'))  # 기본값 01:00


def create_scheduler():
    """스케줄러 생성 및 설정"""
    scheduler = BackgroundScheduler()
    
    # 마라톤 크롤링 작업 등록 (매일 지정된 시간에 실행)
    scheduler.add_job(
        crawl_marathons,
        trigger=CronTrigger(hour=CRAWL_HOUR, minute=CRAWL_MINUTE),
        id='marathon_crawler',
        name='마라톤 정보 크롤링',
        replace_existing=True
    )
    
    return scheduler


def crawl_marathons():
    """마라톤 크롤링 실행 함수"""
    logger.info(f"마라톤 크롤링 작업 시작: {datetime.now()}")
    
    try:
        with MarathonCrawler() as crawler:
            crawler.crawl()
        logger.info("마라톤 크롤링 작업 성공적으로 완료")
    except Exception as e:
        logger.error(f"마라톤 크롤링 작업 실패: {str(e)}")


def start_scheduler():
    """스케줄러 시작"""
    scheduler = create_scheduler()
    
    try:
        scheduler.start()
        logger.info(f"스케줄러 시작 - 마라톤 크롤링 설정: 매일 {CRAWL_HOUR:02d}:{CRAWL_MINUTE:02d}")
        return scheduler
    except Exception as e:
        logger.error(f"스케줄러 시작 실패: {str(e)}")
        return None